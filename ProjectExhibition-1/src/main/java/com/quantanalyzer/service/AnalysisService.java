package com.quantanalyzer.service;


import com.quantanalyzer.domain.Price;
import com.quantanalyzer.dto.AnalysisResponse;
import com.quantanalyzer.dto.RiskMetricsDto;
import com.quantanalyzer.exception.TickerNotFoundException;
import com.quantanalyzer.strategy.StrategyFactor;
import com.quantanalyzer.strategy.TradingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisService {
    private static final double DEFAULT_RISK_FREE_RATE = 0.04; // 4% annual, hardcoded for now

    private final PriceService priceService;
    private final StrategyFactor strategyFactor;
    private final BacktestEngineService backtestEngineService;
    private final RiskCalculatorService riskCalculatorService;

    public AnalysisResponse analyze(String ticker, String benchmarkTicker, String strategySlug) {
        List<Price> bars = priceService.getDailyBars(ticker);
        if (bars.isEmpty()) {
            throw new TickerNotFoundException(ticker);
        }

        TradingStrategy strategy = strategyFactor.resolve(strategySlug);
        BacktestOutcome outcome = backtestEngineService.runFullBacktest(strategy, bars);

        List<Double> benchmarkEquityCurve = null;
        String normalizedBenchmark = null;
        if (benchmarkTicker != null && !benchmarkTicker.isBlank()) {
            List<Price> benchmarkBars = priceService.getDailyBars(benchmarkTicker);
            if (!benchmarkBars.isEmpty()) {
                benchmarkEquityCurve = backtestEngineService.buildBuyAndHoldEquityCurve(benchmarkBars);
                normalizedBenchmark = benchmarkTicker.toUpperCase();
            }
        }

        RiskMetricsDto riskMetrics = riskCalculatorService.computeMetrics(
                outcome.equityCurve(), benchmarkEquityCurve, DEFAULT_RISK_FREE_RATE);

        return AnalysisResponse.builder()
                .ticker(ticker.toUpperCase())
                .benchmark(normalizedBenchmark)
                .fromDate(bars.get(0).getBarDate())
                .toDate(bars.get(bars.size() - 1).getBarDate())
                .barsAnalyzed(bars.size())
                .backtest(outcome.summary())
                .riskMetrics(riskMetrics)
                .build();
    }
}
