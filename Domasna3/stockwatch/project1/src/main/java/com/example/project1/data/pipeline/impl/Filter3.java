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
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class Filter3 implements Filter<List<Issuer>> {

    private final IssuerRepository issuerRepository;
    private final CompanyDataRepository companyDataRepository;

    private static final String HISTORICAL_DATA_URL = "https://www.mse.mk/mk/stats/symbolhistory/";

    public Filter3(IssuerRepository issuerRepository, CompanyDataRepository companyDataRepository) {
        this.issuerRepository = issuerRepository;
        this.companyDataRepository = companyDataRepository;
    }

    public List<Issuer> execute(List<Issuer> input) throws IOException, ParseException {

        for (Issuer company : input) {
            LocalDate fromDate = LocalDate.now();
            LocalDate toDate = LocalDate.now().plusYears(1);
            addHistoricalData(company, fromDate, toDate);
        }

        return null;
    }

    private void addHistoricalData(Issuer company, LocalDate fromDate, LocalDate toDate) throws IOException {
        Connection.Response response = Jsoup.connect(HISTORICAL_DATA_URL + company.getCompanyCode())
                .data("FromDate", fromDate.toString())
                .data("ToDate", toDate.toString())
                .method(Connection.Method.POST)
                .execute();

        Document document = response.parse();

        Element table = document.select("table#resultsTable").first();

        if (table != null) {
            Elements rows = table.select("tbody tr");

            for (Element row : rows) {
                Elements columns = row.select("td");

                if (columns.size() > 0) {
                    LocalDate date = DataTransformer.parseDate(columns.get(0).text(), "d.M.yyyy");

                    if (companyDataRepository.findByDateAndCompany(date, company).isEmpty()) {

                        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);

                        Double lastTransactionPrice = DataTransformer.parseDouble(columns.get(1).text(), format);
                        Double maxPrice = DataTransformer.parseDouble(columns.get(2).text(), format);
                        Double minPrice = DataTransformer.parseDouble(columns.get(3).text(), format);
                        Double averagePrice = DataTransformer.parseDouble(columns.get(4).text(), format);
                        Double percentageChange = DataTransformer.parseDouble(columns.get(5).text(), format);
                        Integer quantity = DataTransformer.parseInteger(columns.get(6).text(), format);
                        Integer turnoverBest = DataTransformer.parseInteger(columns.get(7).text(), format);
                        Integer totalTurnover = DataTransformer.parseInteger(columns.get(8).text(), format);

                        if (maxPrice != null) {

                            if (company.getLastUpdated() == null || company.getLastUpdated().isBefore(date)) {
                                company.setLastUpdated(date);
                            }

                            CompanyData companyData = new CompanyData(
                                    date, lastTransactionPrice, maxPrice, minPrice, averagePrice, percentageChange,
                                    quantity, turnoverBest, totalTurnover);
                            companyData.setCompany(company);
                            companyDataRepository.save(companyData);
                            company.getHistoricalData().add(companyData);
                        }
                    }
                }
            }
        }
        issuerRepository.save(company);
    }


}
