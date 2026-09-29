package com.quantanalyzer.service;

import com.quantanalyzer.dto.BacktestSummaryDto;

import java.util.List;

/**
 * Internal carrier (NOT a public API DTO) holding both the API-facing backtest summary
 * and the raw equity curve, which only RiskCalculatorService needs internally to compute
 * Sharpe Ratio / Max Drawdown / Beta. The equity curve never gets serialized to the client.
 */
public record BacktestOutcome(BacktestSummaryDto summary, List<Double> equityCurve) {
}