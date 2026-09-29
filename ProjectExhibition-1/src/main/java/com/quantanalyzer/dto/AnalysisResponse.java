package com.quantanalyzer.dto;


import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class AnalysisResponse {
    String ticker;
    String benchmark;          // nullable -- null if no benchmark was requested
    LocalDate fromDate;
    LocalDate toDate;
    int barsAnalyzed;
    BacktestSummaryDto backtest;
    RiskMetricsDto riskMetrics;

}
