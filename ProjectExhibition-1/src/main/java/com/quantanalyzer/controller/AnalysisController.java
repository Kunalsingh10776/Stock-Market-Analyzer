package com.quantanalyzer.controller;


import com.quantanalyzer.dto.AnalysisResponse;
import com.quantanalyzer.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor

public class AnalysisController {
    private final AnalysisService analysisService;

    @GetMapping("/analyze/{ticker}")
    public ResponseEntity<AnalysisResponse> analyze(

            @PathVariable String ticker,
            @RequestParam(required = false) String benchmark,
            @RequestParam(defaultValue = "golden-cross") String strategy) {

        AnalysisResponse response = analysisService.analyze(ticker, benchmark, strategy);
        return ResponseEntity.ok(response);
    }
}
