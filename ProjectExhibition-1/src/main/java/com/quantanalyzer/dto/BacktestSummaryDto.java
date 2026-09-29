package com.quantanalyzer.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;


@Value
@Builder
public class BacktestSummaryDto {
    String strategyName;
    int totalTrades;
    long winningTrades;
    double winRatePct;
    double totalReturnPct;
    List<TradeDto> trades;
}
