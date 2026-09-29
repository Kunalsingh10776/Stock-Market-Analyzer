package com.quantanalyzer.service;


import com.quantanalyzer.dto.RiskMetricsDto;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class RiskCalculatorService {

    private static final int TRADING_DAYS_PER_YEAR = 252;

    /**
     * @param equityCurve           the strategy's own equity curve (from BacktestOutcome)
     * @param benchmarkEquityCurve  optional buy-and-hold benchmark curve; pass null to skip Beta
     * @param annualRiskFreeRate    e.g. 0.04 for 4% -- used in the Sharpe Ratio calculation
     */
    public RiskMetricsDto computeMetrics(
            List<Double> equityCurve, List<Double> benchmarkEquityCurve, double annualRiskFreeRate) {

        if (equityCurve == null || equityCurve.size() < 2) {
            throw new IllegalArgumentException("Equity curve needs at least 2 points to compute returns");
        }

        double[] dailyReturns = toPeriodicReturns(equityCurve);

        double sharpe = sharpeRatio(dailyReturns, annualRiskFreeRate);
        double maxDrawdownPct = maxDrawdown(equityCurve) * 100.0;

        Mean mean = new Mean();
        double meanDailyReturn = mean.evaluate(dailyReturns);
        double annualizedReturnPct = (Math.pow(1 + meanDailyReturn, TRADING_DAYS_PER_YEAR) - 1) * 100.0;

        StandardDeviation stdDev = new StandardDeviation();
        double annualizedVolatilityPct = stdDev.evaluate(dailyReturns) * Math.sqrt(TRADING_DAYS_PER_YEAR) * 100.0;

        Double beta = null;
        if (benchmarkEquityCurve != null && benchmarkEquityCurve.size() >= 2) {
            beta = beta(dailyReturns, toPeriodicReturns(benchmarkEquityCurve));
        }

        return RiskMetricsDto.builder()
                .sharpeRatio(sharpe)
                .maxDrawdownPct(maxDrawdownPct)
                .beta(beta)
                .annualizedVolatilityPct(annualizedVolatilityPct)
                .annualizedReturnPct(annualizedReturnPct)
                .build();
    }

    private double[] toPeriodicReturns(List<Double> equityCurve) {
        double[] returns = new double[equityCurve.size() - 1];
        for (int i = 1; i < equityCurve.size(); i++) {
            double prev = equityCurve.get(i - 1);
            double curr = equityCurve.get(i);
            returns[i - 1] = prev == 0 ? 0 : (curr - prev) / prev;
        }
        return returns;
    }

    private double sharpeRatio(double[] dailyReturns, double annualRiskFreeRate) {
        double dailyRiskFreeRate = annualRiskFreeRate / TRADING_DAYS_PER_YEAR;
        double[] excessReturns = new double[dailyReturns.length];
        for (int i = 0; i < dailyReturns.length; i++) {
            excessReturns[i] = dailyReturns[i] - dailyRiskFreeRate;
        }

        double meanExcess = new Mean().evaluate(excessReturns);
        double sd = new StandardDeviation().evaluate(excessReturns);

        return sd == 0.0 ? 0.0 : (meanExcess / sd) * Math.sqrt(TRADING_DAYS_PER_YEAR);
    }

    private double maxDrawdown(List<Double> equityCurve) {
        double peak = equityCurve.get(0);
        double maxDrawdown = 0.0;
        for (double value : equityCurve) {
            if (value > peak) peak = value;
            double drawdown = (peak - value) / peak;
            if (drawdown > maxDrawdown) maxDrawdown = drawdown;
        }
        return maxDrawdown;
    }

    private double beta(double[] strategyReturns, double[] benchmarkReturns) {
        int n = Math.min(strategyReturns.length, benchmarkReturns.length);
        double[] s = Arrays.copyOf(strategyReturns, n);
        double[] b = Arrays.copyOf(benchmarkReturns, n);

        double covariance = new Covariance().covariance(s, b);
        double benchmarkVariance = new Variance().evaluate(b);

        return benchmarkVariance == 0.0 ? 0.0 : covariance / benchmarkVariance;
    }
}
