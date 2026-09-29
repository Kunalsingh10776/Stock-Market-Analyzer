package com.quantanalyzer.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class StrategyFactor {

    private final com.quantanalyzer.strategy.GoldenCrossStrategy goldenCrossStrategy;
    private final com.quantanalyzer.strategy.RsiReversionStrategy rsiReversionStrategy;

    /**
     * @param strategySlug e.g. "golden-cross" or "rsi-reversion" (case-insensitive)
     * @throws InvalidStrategyException if the slug does not match any known strategy
     */
    public com.quantanalyzer.strategy.TradingStrategy resolve(String strategySlug) {
        String normalized = strategySlug == null ? "golden-cross" : strategySlug.toLowerCase().trim();

        return switch (normalized) {
            case "golden-cross" -> goldenCrossStrategy;
            case "rsi-reversion" -> rsiReversionStrategy;
            default -> throw new InvalidStrategyException(
                    "Unknown strategy '" + strategySlug + "'. Supported: golden-cross, rsi-reversion");
        };
    }

    /** Raised when the requested strategy slug does not match any registered strategy. */
    public static class InvalidStrategyException extends RuntimeException {
        public InvalidStrategyException(String message) {
            super(message);
        }
    }
}