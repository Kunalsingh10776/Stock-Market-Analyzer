package com.quantanalyzer.exception;

public class TickerNotFoundException extends RuntimeException{

    public TickerNotFoundException(String ticker) {
        super("Ticker not found for :  " + ticker);
    }
}
