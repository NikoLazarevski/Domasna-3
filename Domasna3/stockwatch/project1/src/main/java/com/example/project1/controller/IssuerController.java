package com.example.project1.controller;

import com.example.project1.entity.Issuer;
import com.example.project1.entity.CompanyData;
import com.example.project1.service.IssuerService;
import com.example.project1.service.CompanyPricePredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class IssuerController {

    private final IssuerService issuerService;
    private final CompanyPricePredictionService companyPricePredictionService;

    @GetMapping("/companies")
    public String getCompaniesPage(Model model) {
        model.addAttribute("companies", issuerService.findAll());
        return "companies";
    }

    @GetMapping("/company")
    public String getCompanyPage(@RequestParam(name = "companyId") Long companyId, Model model) throws Exception {
        List<Map<String, Object>> companyData = new ArrayList<>();
        Issuer company = issuerService.findById(companyId);

        Map<String, Object> data = new HashMap<>();
        data.put("companyCode", company.getCompanyCode());
        data.put("lastUpdated", company.getLastUpdated());

        List<LocalDate> dates = new ArrayList<>();
        List<Double> prices = new ArrayList<>();

        for (CompanyData historicalData : company.getHistoricalData()) {
            dates.add(historicalData.getDate());
            prices.add(historicalData.getLastTransactionPrice());
        }

        data.put("dates", dates);
        data.put("prices", prices);
        data.put("id", company.getId());
        companyData.add(data);

        model.addAttribute("companyData", companyData);
        return "company";
    }

}