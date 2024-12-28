package com.example.project1.data.pipeline.impl;

import com.example.project1.data.DataTransformer;
import com.example.project1.data.pipeline.Filter;
import com.example.project1.entity.Issuer;
import com.example.project1.entity.CompanyData;
import com.example.project1.repository.IssuerRepository;
import com.example.project1.repository.CompanyDataRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Filter2 implements Filter<List<Issuer>> {

    private final IssuerRepository issuerRepository;
    private final CompanyDataRepository companyDataRepository;

    private static final String HISTORICAL_DATA_URL = "https://www.mse.mk/mk/stats/symbolhistory/";

    public Filter2(IssuerRepository issuerRepository, CompanyDataRepository companyDataRepository) {
        this.issuerRepository = issuerRepository;
        this.companyDataRepository = companyDataRepository;
    }

    public List<Issuer> execute(List<Issuer> input) throws IOException {
        List<Issuer> updatedIssuers = new ArrayList<>();

        for (Issuer issuer : input) {
            if (issuer.getLastUpdated() == null) {
                populateHistoricalData(issuer);
            } else {
                updatedIssuers.add(issuer);
            }
        }

        return updatedIssuers;
    }

    private void populateHistoricalData(Issuer issuer) throws IOException {
        for (int yearOffset = 1; yearOffset <= 10; yearOffset++) {
            LocalDate startDate = LocalDate.now().minusYears(yearOffset);
            LocalDate endDate = LocalDate.now().minusYears(yearOffset - 1);
            fetchAndSaveHistoricalData(issuer, startDate, endDate);
        }
    }

    private void fetchAndSaveHistoricalData(Issuer issuer, LocalDate fromDate, LocalDate toDate) throws IOException {
        Connection.Response response = Jsoup.connect(HISTORICAL_DATA_URL + issuer.getCompanyCode())
                .data("FromDate", fromDate.toString())
                .data("ToDate", toDate.toString())
                .method(Connection.Method.POST)
                .execute();

        Document document = response.parse();
        Element resultsTable = document.select("table#resultsTable").first();

        if (resultsTable != null) {
            processTableRows(resultsTable.select("tbody tr"), issuer);
        }

        issuerRepository.save(issuer);
    }

    private void processTableRows(Elements rows, Issuer issuer) {
        for (Element row : rows) {
            Elements columns = row.select("td");

            if (!columns.isEmpty()) {
                LocalDate recordDate = DataTransformer.parseDate(columns.get(0).text(), "d.M.yyyy");

                if (companyDataRepository.findByDateAndCompany(recordDate, issuer).isEmpty()) {
                    saveCompanyData(columns, issuer, recordDate);
                }
            }
        }
    }

    private void saveCompanyData(Elements columns, Issuer issuer, LocalDate recordDate) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.GERMANY);

        Double lastPrice = DataTransformer.parseDouble(columns.get(1).text(), formatter);
        Double highPrice = DataTransformer.parseDouble(columns.get(2).text(), formatter);
        Double lowPrice = DataTransformer.parseDouble(columns.get(3).text(), formatter);
        Double avgPrice = DataTransformer.parseDouble(columns.get(4).text(), formatter);
        Double percentChange = DataTransformer.parseDouble(columns.get(5).text(), formatter);
        Integer volume = DataTransformer.parseInteger(columns.get(6).text(), formatter);
        Integer turnoverBest = DataTransformer.parseInteger(columns.get(7).text(), formatter);
        Integer totalTurnover = DataTransformer.parseInteger(columns.get(8).text(), formatter);

        if (highPrice != null) {
            updateLastUpdatedDate(issuer, recordDate);

            CompanyData companyData = new CompanyData(
                    recordDate, lastPrice, highPrice, lowPrice, avgPrice, percentChange,
                    volume, turnoverBest, totalTurnover);
            companyData.setCompany(issuer);

            companyDataRepository.save(companyData);
            issuer.getHistoricalData().add(companyData);
        }
    }

    private void updateLastUpdatedDate(Issuer issuer, LocalDate recordDate) {
        if (issuer.getLastUpdated() == null || issuer.getLastUpdated().isBefore(recordDate)) {
            issuer.setLastUpdated(recordDate);
        }
    }
}
