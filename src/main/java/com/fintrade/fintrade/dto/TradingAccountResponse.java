package com.fintrade.fintrade.dto;

import com.fintrade.fintrade.entity.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class TradingAccountResponse {

    private Long id;
    private String accountName;
    private String broker;
    private String currency;
    private BigDecimal balance;
    private AccountStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public TradingAccountResponse() {
    }

    public TradingAccountResponse(
            Long id,
            String accountName,
            String broker,
            String currency,
            BigDecimal balance,
            AccountStatus status,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.accountName = accountName;
        this.broker = broker;
        this.currency = currency;
        this.balance = balance;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getBroker() {
        return broker;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
