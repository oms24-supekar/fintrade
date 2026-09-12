package com.fintrade.fintrade.service;

import com.fintrade.fintrade.dto.MarketQuoteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
public class MarketQuoteService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MarketQuoteService(
            @Value("${app.market-data.base-url}") String baseUrl,
            ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
    }

    public Optional<MarketQuoteResponse> findQuote(String requestedSymbol) {
        String symbol = requestedSymbol.trim().toUpperCase(Locale.ROOT);

        try {
            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v8/finance/chart/{symbol}")
                            .queryParam("range", "1d")
                            .queryParam("interval", "1m")
                            .build(symbol))
                    .retrieve()
                    .body(String.class);

            JsonNode metadata = objectMapper.readTree(response)
                    .path("chart")
                    .path("result")
                    .path(0)
                    .path("meta");
            String priceText = metadata.path("regularMarketPrice").asText();
            if (priceText.isBlank()) {
                return Optional.empty();
            }

            BigDecimal price = new BigDecimal(priceText);
            String currency = metadata.path("currency").asText("USD");
            long epochSeconds = metadata.path("regularMarketTime").asLong(Instant.now().getEpochSecond());

            return Optional.of(new MarketQuoteResponse(
                    symbol,
                    price,
                    currency,
                    Instant.ofEpochSecond(epochSeconds),
                    "YAHOO_FINANCE"));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
