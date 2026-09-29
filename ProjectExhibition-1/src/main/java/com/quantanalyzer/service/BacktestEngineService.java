package com.quantanalyzer.service;

import com.quantanalyzer.domain.Price;
import com.quantanalyzer.dto.BacktestSummaryDto;
import com.quantanalyzer.dto.TradeDto;
import com.quantanalyzer.strategy.TradingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.num.DecimalNum;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts raw Price data into Ta4j's BarSeries format, and runs trading strategies
 * against it -- either for a single "current signal" (existing behaviour) or a full
 * historical backtest producing a trade log and equity curve (new behaviour).
 */
@Service
@RequiredArgsConstructor
public class BacktestEngineService {

    /** Existing method -- gives today's BUY/SELL/HOLD signal based on the last bar only. */
    public String getCurrentSignal(TradingStrategy tradingStrategy, List<Price> priceHistory) {
        BarSeries series = buildSeries(priceHistory);
        Strategy strategy = tradingStrategy.buildStrategy(series);
        int lastIndex = series.getEndIndex();

        if (strategy.shouldEnter(lastIndex)){
            return "BUY";
        }
        if (strategy.shouldExit(lastIndex)){
            return "SELL";
        }
        return "HOLD";
    }

    /**
     * Runs the given strategy over the ENTIRE price history (not just the last bar),
     * producing every historical trade plus a day-by-day equity curve.
     */
    public BacktestOutcome runFullBacktest(TradingStrategy tradingStrategy, List<Price> priceHistory) {
        BarSeries series = buildSeries(priceHistory);
        Strategy strategy = tradingStrategy.buildStrategy(series);

        BarSeriesManager manager = new BarSeriesManager(series);
        TradingRecord tradingRecord = manager.run(strategy);

        List<TradeDto> trades = new ArrayList<>();
        long wins = 0;

        for (Position position : tradingRecord.getPositions()) {
            if (!position.isClosed()) {
                continue;
            }
            double entryPrice = position.getEntry().getNetPrice().doubleValue();
            double exitPrice = position.getExit().getNetPrice().doubleValue();
            double returnPct = (exitPrice - entryPrice) / entryPrice * 100.0;
            boolean win = returnPct > 0;
            if (win) wins++;

            LocalDate entryDate = priceHistory.get(position.getEntry().getIndex()).getBarDate();
            LocalDate exitDate = priceHistory.get(position.getExit().getIndex()).getBarDate();

            trades.add(TradeDto.builder()
                    .entryDate(entryDate)
                    .entryPrice(entryPrice)
                    .exitDate(exitDate)
                    .exitPrice(exitPrice)
                    .returnPct(returnPct)
                    .winningTrade(win)
                    .build());
        }

        List<Double> equityCurve = buildEquityCurve(priceHistory, tradingRecord);
        double totalReturnPct = (equityCurve.get(equityCurve.size() - 1) - 1.0) * 100.0;
        int totalTrades = trades.size();
        double winRatePct = totalTrades == 0 ? 0.0 : (wins * 100.0 / totalTrades);

        BacktestSummaryDto summary = BacktestSummaryDto.builder()
                .strategyName(tradingStrategy.getName())
                .totalTrades(totalTrades)
                .winningTrades(wins)
                .winRatePct(winRatePct)
                .totalReturnPct(totalReturnPct)
                .trades(trades)
                .build();

        return new BacktestOutcome(summary, equityCurve);
    }

    /**
     * A simple "hold from day 1 to the last day" equity curve -- no strategy applied.
     * Used specifically for the BENCHMARK's returns when computing Beta, since Beta should
     * compare your strategy against the market's natural movement, not against the
     * benchmark run through the same strategy.
     */
    public List<Double> buildBuyAndHoldEquityCurve(List<Price> priceHistory) {
        List<Double> equity = new ArrayList<>(priceHistory.size());
        double equityValue = 1.0;
        equity.add(equityValue);

        for (int i = 1; i < priceHistory.size(); i++) {
            double prevClose = priceHistory.get(i - 1).getClosePrice().doubleValue();
            double currClose = priceHistory.get(i).getClosePrice().doubleValue();
            equityValue *= (1 + (currClose - prevClose) / prevClose);
            equity.add(equityValue);
        }
        return equity;
    }

    /** Marks the position to market on every bar it's open; flat (no change) while out of the market. */
    private List<Double> buildEquityCurve(List<Price> priceHistory, TradingRecord tradingRecord) {
        List<Double> equity = new ArrayList<>(priceHistory.size());
        double equityValue = 1.0;
        equity.add(equityValue);

        for (int i = 1; i < priceHistory.size(); i++) {
            final int currentIndex = i;
            boolean inPosition = tradingRecord.getPositions().stream()
                    .anyMatch(p -> p.isClosed()
                            && p.getEntry().getIndex() < currentIndex
                            && p.getExit().getIndex() >= currentIndex);

            if (inPosition) {
                double prevClose = priceHistory.get(i - 1).getClosePrice().doubleValue();
                double currClose = priceHistory.get(i).getClosePrice().doubleValue();
                equityValue *= (1 + (currClose - prevClose) / prevClose);
            }
            equity.add(equityValue);
        }
        return equity;
    }

    /** Converts a chronologically-sorted Price list into a Ta4j BarSeries. */
    private BarSeries buildSeries(List<Price> priceHistory) {
        BaseBarSeries series = new BaseBarSeriesBuilder()
                .withName("series")
                .withNumTypeOf(DecimalNum.class)
                .build();

        for (Price bar : priceHistory) {
            series.addBar(
                    bar.getBarDate().atStartOfDay(ZoneOffset.UTC),
                    DecimalNum.valueOf(bar.getOpenPrice()),
                    DecimalNum.valueOf(bar.getHighPrice()),
                    DecimalNum.valueOf(bar.getLowPrice()),
                    DecimalNum.valueOf(bar.getClosePrice()),
                    DecimalNum.valueOf(bar.getVolume())
            );
        }
        return series;
    }
}