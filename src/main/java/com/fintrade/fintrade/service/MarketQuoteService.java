package com.fintrade.fintrade.service;

import com.fintrade.fintrade.dto.MarketCandleResponse;
import com.fintrade.fintrade.dto.MarketQuoteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class MarketQuoteService implements MarketDataProvider {

    private static final Set<String> SUPPORTED_RESOLUTIONS = Set.of(
            "1m",
            "3m",
            "5m",
            "15m",
            "30m",
            "1h",
            "2h",
            "4h",
            "6h",
            "1d",
            "1w"
    );

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MarketQuoteService(
            @Value("${app.market-data.base-url}") String baseUrl,
            ObjectMapper objectMapper) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<MarketQuoteResponse> findQuote(String requestedSymbol) {

        String symbol = normalizeSymbol(requestedSymbol);

        try {

            String response = restClient.get()
                    .uri("/v2/tickers/{symbol}", symbol)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);

            if (!root.path("success").asBoolean()) {
                return Optional.empty();
            }

            JsonNode result = root.path("result");

            String priceText = firstNonBlank(
                    result.path("close").asText(),
                    result.path("mark_price").asText(),
                    result.path("spot_price").asText()
            );

            if (priceText == null) {
                return Optional.empty();
            }

            BigDecimal price = new BigDecimal(priceText);

            return Optional.of(
                    new MarketQuoteResponse(
                            symbol,
                            price,
                            "USD",
                            Instant.now(),
                            "DELTA_EXCHANGE"
                    )
            );

        } catch (Exception ex) {

            return Optional.empty();
        }
    }

    @Override
    public List<MarketCandleResponse> getCandles(
            String requestedSymbol,
            String requestedResolution,
            long startEpochSeconds,
            long endEpochSeconds) {

        String symbol = normalizeSymbol(requestedSymbol);
        String resolution = normalizeResolution(requestedResolution);

        if (startEpochSeconds <= 0) {
            throw new IllegalArgumentException(
                    "Start timestamp must be greater than zero"
            );
        }

        if (endEpochSeconds <= 0) {
            throw new IllegalArgumentException(
                    "End timestamp must be greater than zero"
            );
        }

        if (startEpochSeconds >= endEpochSeconds) {
            throw new IllegalArgumentException(
                    "Start timestamp must be before end timestamp"
            );
        }

        try {

            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/history/candles")
                            .queryParam("symbol", symbol)
                            .queryParam("resolution", resolution)
                            .queryParam("start", startEpochSeconds)
                            .queryParam("end", endEpochSeconds)
                            .build())
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);

            if (!root.path("success").asBoolean()) {
                throw new IllegalArgumentException(
                        "Delta Exchange rejected candle request"
                );
            }

            JsonNode result = root.path("result");

            List<MarketCandleResponse> candles =
                    new ArrayList<>();

            for (JsonNode candle : result) {

                MarketCandleResponse responseCandle =
                        new MarketCandleResponse(
                                symbol,
                                resolution,
                                Instant.ofEpochSecond(
                                        candle.path("time").asLong()
                                ),
                                decimal(candle, "open"),
                                decimal(candle, "high"),
                                decimal(candle, "low"),
                                decimal(candle, "close"),
                                decimal(candle, "volume"),
                                "DELTA_EXCHANGE"
                        );

                candles.add(responseCandle);
            }

            return candles;

        } catch (IllegalArgumentException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Unable to fetch market candles from Delta Exchange",
                    ex
            );
        }
    }

    private String normalizeSymbol(String symbol) {

        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException(
                    "Symbol is required"
            );
        }

        return symbol
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeResolution(String resolution) {

        if (resolution == null || resolution.isBlank()) {
            throw new IllegalArgumentException(
                    "Resolution is required"
            );
        }

        String normalized =
                resolution.trim().toLowerCase(Locale.ROOT);

        if (!SUPPORTED_RESOLUTIONS.contains(normalized)) {
            throw new IllegalArgumentException(
                    "Unsupported resolution: " + resolution
            );
        }

        return normalized;
    }

    private BigDecimal decimal(
            JsonNode node,
            String field) {

        String value =
                node.path(field).asText();

        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(value);
    }

    private String firstNonBlank(String... values) {

        for (String value : values) {

            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }
}