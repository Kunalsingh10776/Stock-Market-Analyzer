package com.quantanalyzer.strategy;

import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

/**
 * Contrarian (mean-reversion) strategy based on the Relative Strength Index (RSI).
 *
 * <p>Enters (BUY) when RSI drops below the oversold threshold (30) -- betting the price
 * has fallen too far, too fast, and will bounce back. Exits (SELL) when RSI rises above
 * the overbought threshold (70) -- betting the price has risen too far and will pull back.
 */
@Component
public class RsiReversionStrategy implements TradingStrategy {

    private static final int RSI_PERIOD = 14;
    private static final double OVERSOLD_THRESHOLD = 30;
    private static final double OVERBOUGHT_THRESHOLD = 70;

    @Override
    public String getName() {
        return "RsiReversion(" + RSI_PERIOD + "," + (int) OVERSOLD_THRESHOLD + "," + (int) OVERBOUGHT_THRESHOLD + ")";
    }

    @Override
    public Strategy buildStrategy(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, RSI_PERIOD);

        Rule entryRule = new UnderIndicatorRule(rsi, OVERSOLD_THRESHOLD);
        Rule exitRule = new OverIndicatorRule(rsi, OVERBOUGHT_THRESHOLD);

        return new BaseStrategy(getName(), entryRule, exitRule);
    }
}