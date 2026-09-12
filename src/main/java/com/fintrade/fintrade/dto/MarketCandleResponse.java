package com.fintrade.fintrade.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class MarketCandleResponse {

    private String symbol;
    private String resolution;
    private Instant timestamp;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal volume;
    private String source;

    public MarketCandleResponse() {
    }

    public MarketCandleResponse(
            String symbol,
            String resolution,
            Instant timestamp,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            BigDecimal volume,
            String source) {

        this.symbol = symbol;
        this.resolution = resolution;
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.source = source;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getResolution() {
        return resolution;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public String getSource() {
        return source;
    }
}