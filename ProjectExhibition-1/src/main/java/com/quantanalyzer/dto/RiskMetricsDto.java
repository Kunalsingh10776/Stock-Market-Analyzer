package com.quantanalyzer.dto;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class RiskMetricsDto {

    double sharpeRatio;
    double maxDrawdownPct;

    Double beta;

    double annualizedVolatilityPct;
    double annualizedReturnPct;
}
