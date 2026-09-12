package com.fintrade.fintrade.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class MarketQuoteResponse {

    private String symbol;
    private BigDecimal price;
    private String currency;
    private Instant asOf;
    private String source;

    public MarketQuoteResponse() {
    }

    public MarketQuoteResponse(
            String symbol,
            BigDecimal price,
            String currency,
            Instant asOf,
            String source) {
        this.symbol = symbol;
        this.price = price;
        this.currency = currency;
        this.asOf = asOf;
        this.source = source;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public Instant getAsOf() {
        return asOf;
    }

    public String getSource() {
        return source;
    }
}
