package com.example.project1.data.pipeline.impl;

import com.example.project1.data.pipeline.Filter;
import com.example.project1.entity.Issuer;
import com.example.project1.repository.IssuerRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class Filter1 implements Filter<List<Issuer>> {

    private final IssuerRepository issuerRepository;

    public Filter1(IssuerRepository issuerRepository) {
        this.issuerRepository = issuerRepository;
    }

    private static final String MARKET_DATA_URL = "https://www.mse.mk/mk/stats/symbolhistory/kmb";

    @Override
    public List<Issuer> execute(List<Issuer> input) throws IOException {
        Document pageContent = Jsoup.connect(MARKET_DATA_URL).get();
        Element dropdownMenu = pageContent.select("select#Code").first();

        if (dropdownMenu != null) {
            Elements dropdownOptions = dropdownMenu.select("option");
            for (Element option : dropdownOptions) {
                String companyCode = option.attr("value");
                if (isValidCompanyCode(companyCode)) {
                    issuerRepository.findByCompanyCode(companyCode)
                            .orElseGet(() -> issuerRepository.save(new Issuer(companyCode)));
                }
            }
        }

        return issuerRepository.findAll();
    }

    private boolean isValidCompanyCode(String code) {
        return code != null && !code.isBlank() && code.matches("^[a-zA-Z]+$");
    }
}
