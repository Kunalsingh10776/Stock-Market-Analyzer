package com.quantanalyzer.controller;

import com.quantanalyzer.domain.Price;
import com.quantanalyzer.service.PriceService;
import com.quantanalyzer.strategy.GoldenCrossStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceService priceService;
    private final GoldenCrossStrategy goldenCrossStrategy;

    @GetMapping("/{ticker}")
    public List<Price> getPrices(@PathVariable String ticker) {
        return priceService.getDailyBars(ticker);
    }

    @GetMapping("/{ticker}/signal")
    public String getSignal(@PathVariable String ticker) {
        List<Price> history = priceService.getDailyBars(ticker);
        return goldenCrossStrategy.generateSignal(history);
    }
}