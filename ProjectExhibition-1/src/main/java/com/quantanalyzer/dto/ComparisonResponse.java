package com.quantanalyzer.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Value;


@Builder
@Value
public class ComparisonResponse {
    String ticker;
    String benchmark;
    LocalDate fromDate;
    LocalDate toDate;
    int barsAnalyzed;
    List<StrategyResultDto> results;
    String recommendation;
}
