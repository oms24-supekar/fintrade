package com.fintrade.fintrade.dto;

import java.math.BigDecimal;
import java.util.List;

public class PortfolioSummaryResponse {

    private Long accountId;
    private String currency;
    private BigDecimal cashBalance;
    private BigDecimal positionCostBasis;
    private BigDecimal positionMarketValue;
    private BigDecimal totalEquity;
    private int quotesAvailable;
    private List<PositionResponse> positions;

    public PortfolioSummaryResponse() {
    }

    public PortfolioSummaryResponse(
            Long accountId,
            String currency,
            BigDecimal cashBalance,
            BigDecimal positionCostBasis,
            BigDecimal positionMarketValue,
            BigDecimal totalEquity,
            int quotesAvailable,
            List<PositionResponse> positions) {
        this.accountId = accountId;
        this.currency = currency;
        this.cashBalance = cashBalance;
        this.positionCostBasis = positionCostBasis;
        this.positionMarketValue = positionMarketValue;
        this.totalEquity = totalEquity;
        this.quotesAvailable = quotesAvailable;
        this.positions = positions;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public BigDecimal getPositionCostBasis() {
        return positionCostBasis;
    }

    public BigDecimal getPositionMarketValue() {
        return positionMarketValue;
    }

    public BigDecimal getTotalEquity() {
        return totalEquity;
    }

    public int getQuotesAvailable() {
        return quotesAvailable;
    }

    public List<PositionResponse> getPositions() {
        return positions;
    }
}
