package com.quantanalyzer.service;

import com.quantanalyzer.client.AlphaVantageClient;
import com.quantanalyzer.domain.Price;
import com.quantanalyzer.repo.BarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implements a cache-aside pattern for price data: consumers ask for a ticker's history,
 * and this service only reaches out to {@link AlphaVantageClient} (the external provider)
 * when nothing is cached yet in the database. Once fetched, bars are persisted so future
 * requests never re-hit the external API for the same ticker.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceService {

    private final AlphaVantageClient alphaVantageClient;
    private final BarRepository priceBarRepository;

    /**
     * Returns daily OHLCV bars for {@code ticker}, ordered oldest-to-newest. Fetches and
     * caches from the external provider on the first request for a given ticker only.
     */
    public List<Price> getDailyBars(String ticker) {
        String normalized = ticker.trim().toUpperCase();

        if (!priceBarRepository.existsByTicker(normalized)) {
            log.info("No cached data for {}, fetching from provider", normalized);
            List<Price> fetched = alphaVantageClient.fetchDailySeries(normalized);
            priceBarRepository.saveAll(fetched);
            log.info("Cached {} bars for {}", fetched.size(), normalized);
        } else {
            log.debug("Serving {} from cache", normalized);
        }

        return priceBarRepository.findByTickerOrderByBarDateAsc(normalized);
    }
}