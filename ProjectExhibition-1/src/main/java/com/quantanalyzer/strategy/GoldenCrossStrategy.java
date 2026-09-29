package com.quantanalyzer.strategy;

import com.quantanalyzer.domain.Price;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Classic Golden Cross / Death Cross strategy: enters when the short-term moving
 * average crosses above the long-term moving average, exits when it crosses back below.
 */
@Component
public class GoldenCrossStrategy implements TradingStrategy {

    private static final int SHORT_PERIOD = 50;
    private static final int LONG_PERIOD = 200;

    @Override
    public String getName() {
        return "GoldenCross(" + SHORT_PERIOD + "," + LONG_PERIOD + ")";
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, SHORT_PERIOD);
        SMAIndicator longSma = new SMAIndicator(closePrice, LONG_PERIOD);

        var entryRule = new CrossedUpIndicatorRule(shortSma, longSma);
        var exitRule = new CrossedDownIndicatorRule(shortSma, longSma);

        return new BaseStrategy(getName(), entryRule, exitRule);
    }

    /**
     * Returns the latest Golden Cross signal for price bars ordered oldest-to-newest.
     * A crossover needs both the current and previous moving averages, so at least
     * {@value LONG_PERIOD} + 1 observations are required.
     */
    public String generateSignal(List<Price> history) {
        if (history == null || history.size() <= LONG_PERIOD) {
            int availableBars = history == null ? 0 : history.size();
            return "HOLD - Need at least " + (LONG_PERIOD + 1)
                    + " daily bars to detect a 50/200 crossover; only "
                    + availableBars + " are available.";
        }

        BigDecimal currentShortAverage = averageClose(history, history.size() - SHORT_PERIOD, history.size());
        BigDecimal currentLongAverage = averageClose(history, history.size() - LONG_PERIOD, history.size());
        BigDecimal previousShortAverage = averageClose(history, history.size() - SHORT_PERIOD - 1, history.size() - 1);
        BigDecimal previousLongAverage = averageClose(history, history.size() - LONG_PERIOD - 1, history.size() - 1);

        if (previousShortAverage.compareTo(previousLongAverage) <= 0
                && currentShortAverage.compareTo(currentLongAverage) > 0) {
            return "BUY - SMA-50 crossed above SMA-200 today ("
                    + currentShortAverage + " > " + currentLongAverage + ").";
        }
        if (previousShortAverage.compareTo(previousLongAverage) >= 0
                && currentShortAverage.compareTo(currentLongAverage) < 0) {
            return "SELL - SMA-50 crossed below SMA-200 today ("
                    + currentShortAverage + " < " + currentLongAverage + ").";
        }

        String trend = currentShortAverage.compareTo(currentLongAverage) > 0
                ? "SMA-50 remains above SMA-200 (bullish trend, but no new crossover today)"
                : currentShortAverage.compareTo(currentLongAverage) < 0
                ? "SMA-50 remains below SMA-200 (bearish trend, but no new crossover today)"
                : "SMA-50 equals SMA-200 (no crossover today)";
        return "HOLD - " + trend + ": " + currentShortAverage + " vs " + currentLongAverage + ".";
    }

    private BigDecimal averageClose(List<Price> history, int startInclusive, int endExclusive) {
        BigDecimal total = BigDecimal.ZERO;
        for (int index = startInclusive; index < endExclusive; index++) {
            total = total.add(history.get(index).getClosePrice());
        }
        return total.divide(BigDecimal.valueOf(endExclusive - startInclusive), 6, RoundingMode.HALF_UP);
    }
}
