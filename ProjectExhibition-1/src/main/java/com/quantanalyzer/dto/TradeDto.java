package com.quantanalyzer.dto;


import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class TradeDto {
    LocalDate entryDate;
    double entryPrice;
    LocalDate exitDate;
    double exitPrice;
    double returnPct;
    boolean winningTrade;
}
