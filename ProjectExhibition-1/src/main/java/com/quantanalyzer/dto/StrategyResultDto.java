package com.quantanalyzer.dto;


import lombok.Builder;
import lombok.Value;


@Value
@Builder

public class StrategyResultDto {
    String strategyName;
    BacktestSummaryDto backtest;
    RiskMetricsDto riskMetrics;
}
