package com.quantanalyzer.strategy;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

/**
 * Contract that every trading strategy must implement. This is the Strategy design
 * pattern: {@code BacktestEngineService} only ever depends on this interface, never on a
 * concrete strategy class -- so adding a brand-new strategy later means writing one new
 * class here and never touching the backtest engine itself.
 */
public interface TradingStrategy {

    /** A human-readable name for this strategy, e.g. "GoldenCross(50,200)". */
    String getName();

    /**
     * Builds a Ta4j {@link Strategy} (entry/exit rules) bound to the given price series.
     * A fresh strategy must be built per series since Ta4j rules are tied to one BarSeries.
     */
    Strategy buildStrategy(BarSeries series);
}