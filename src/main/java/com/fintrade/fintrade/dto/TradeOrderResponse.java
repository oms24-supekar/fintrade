package com.fintrade.fintrade.dto;

import com.fintrade.fintrade.entity.OrderStatus;
import com.fintrade.fintrade.entity.TradeSide;

import java.math.BigDecimal;
import java.time.Instant;

public class TradeOrderResponse {

    private Long id;
    private String symbol;
    private TradeSide side;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalValue;
    private OrderStatus status;
    private Instant createdAt;

    public TradeOrderResponse() {
    }

    public TradeOrderResponse(
            Long id,
            String symbol,
            TradeSide side,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal totalValue,
            OrderStatus status,
            Instant createdAt) {
        this.id = id;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.totalValue = totalValue;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public TradeSide getSide() {
        return side;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
