package com.fintrade.fintrade.dto;

import com.fintrade.fintrade.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountTransactionResponse {

    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private Instant createdAt;

    public AccountTransactionResponse() {
    }

    public AccountTransactionResponse(
            Long id,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String description,
            Instant createdAt) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
