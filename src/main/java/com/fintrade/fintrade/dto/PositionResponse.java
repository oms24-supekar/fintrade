package com.fintrade.fintrade.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class PositionResponse {

    private Long id;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal averagePrice;
    private Instant updatedAt;

    public PositionResponse() {
    }

    public PositionResponse(
            Long id,
            String symbol,
            BigDecimal quantity,
            BigDecimal averagePrice,
            Instant updatedAt) {
        this.id = id;
        this.symbol = symbol;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getAveragePrice() {
        return averagePrice;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
