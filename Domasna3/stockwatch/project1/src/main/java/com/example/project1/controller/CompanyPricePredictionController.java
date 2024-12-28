package com.example.project1.controller;

import com.example.project1.entity.dto.NLPResponse;
import com.example.project1.service.CompanyPricePredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CompanyPricePredictionController {

    private final CompanyPricePredictionService companyPricePredictionService;

    @PostMapping("/predict")
    public ResponseEntity<String> technicalAnalysis(@RequestParam(name = "companyId") Long companyId) {
        String response = companyPricePredictionService.technicalAnalysis(companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analyze")
    public ResponseEntity<NLPResponse> nlp(@RequestParam(name = "companyId") Long companyId) throws Exception {
        NLPResponse response = companyPricePredictionService.nlp(companyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/predict-next-month-price")
    public ResponseEntity<Double> lstm(@RequestParam(name = "companyId") Long companyId) {
        Double response = companyPricePredictionService.lstm(companyId);
        return ResponseEntity.ok(response);
    }
}
