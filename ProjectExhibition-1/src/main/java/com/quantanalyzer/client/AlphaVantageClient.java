package com.quantanalyzer.client;

//This file is responsible for communicating with the Alpha Vantage external financial data API over HTTP.
//	Fetches live data via HTTP, or falls back to bundled sample JSON if no API key/offline
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantanalyzer.domain.Price;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Talks to the Alpha Vantage {@code TIME_SERIES_DAILY} endpoint to fetch historical OHLCV
 * data for a ticker. Falls back to bundled sample JSON files under
 * {@code resources/sample-data/} when no API key is configured or the network/provider is
 * unavailable, so the application always has something to run against.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlphaVantageClient {

    private static final String BASE_URL = "https://www.alphavantage.co/query";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${marketdata.alphavantage.api-key:demo}")
    private String apiKey;

    /**
     * Fetches daily OHLCV bars for a ticker. Tries the live Alpha Vantage API first (unless
     * the key is left as "demo"); if that fails or is unavailable, loads bundled sample
     * data from the classpath so local development and tests never depend on the network.
     */
    public List<Price> fetchDailySeries(String ticker) {
        if (!"demo".equalsIgnoreCase(apiKey)) {
            try {
                return fetchFromLiveApi(ticker);
            } catch (Exception e) {
                log.warn("Live Alpha Vantage fetch failed for {}, falling back to sample data: {}",
                        ticker, e.getMessage());
            }
        }
        return loadSampleData(ticker);
    }

    private List<Price> fetchFromLiveApi(String ticker) throws IOException, InterruptedException {
        String url = String.format(
                "%s?function=TIME_SERIES_DAILY&symbol=%s&outputsize=full&apikey=%s",
                BASE_URL, ticker, apiKey);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new ExternalApiException("Alpha Vantage returned HTTP " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());

        // Alpha Vantage returns HTTP 200 even when rate-limited or given a bad request --
        // the only signal is a "Note" or "Information" field instead of the real payload.
        if (root.has("Note") || root.has("Information")) {
            throw new ExternalApiException(
                    "Alpha Vantage rate-limited or rejected the request: "
                            + root.path("Note").asText(root.path("Information").asText()));
        }

        JsonNode series = root.get("Time Series (Daily)");
        if (series == null || series.isEmpty()) {
            throw new ExternalApiException("No time series data returned for ticker: " + ticker);
        }

        return parseSeries(ticker, series);
    }

    /** Loads bundled sample OHLCV data (e.g. resources/sample-data/AAPL.json) as a fallback. */
    private List<Price> loadSampleData(String ticker) {
        String resourcePath = "sample-data/" + ticker.toUpperCase() + ".json";
        try (InputStream is = new ClassPathResource(resourcePath).getInputStream()) {
            JsonNode root = objectMapper.readTree(is);
            JsonNode series = root.get("Time Series (Daily)");
            if (series == null) {
                throw new ExternalApiException("Sample data file for " + ticker + " is malformed");
            }
            return parseSeries(ticker.toUpperCase(), series);
        } catch (IOException e) {
            throw new ExternalApiException("No sample data bundled for ticker: " + ticker, e);
        }
    }

    private List<Price> parseSeries(String ticker, JsonNode series) {
        List<Price> bars = new ArrayList<>();
        Iterator<String> dateKeys = series.fieldNames();

        while (dateKeys.hasNext()) {
            String dateKey = dateKeys.next();
            JsonNode ohlcv = series.get(dateKey);

            bars.add(Price.builder()
                    .ticker(ticker)
                    .barDate(LocalDate.parse(dateKey))
                    .openPrice(new BigDecimal(ohlcv.get("1. open").asText()))
                    .highPrice(new BigDecimal(ohlcv.get("2. high").asText()))
                    .lowPrice(new BigDecimal(ohlcv.get("3. low").asText()))
                    .closePrice(new BigDecimal(ohlcv.get("4. close").asText()))
                    .volume(ohlcv.get("5. volume").asLong())
                    .build());
        }
        return bars;
    }

    /** Raised when the external market data provider fails or returns unusable data. */
    public static class ExternalApiException extends RuntimeException {
        public ExternalApiException(String message) {
            super(message);
        }

        public ExternalApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}