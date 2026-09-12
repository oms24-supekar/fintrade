package com.fintrade.fintrade.service;

import com.fintrade.fintrade.dto.MarketCandleResponse;
import com.fintrade.fintrade.dto.MarketQuoteResponse;

import java.util.List;
import java.util.Optional;

public interface MarketDataProvider {

    Optional<MarketQuoteResponse> findQuote(String symbol);

    List<MarketCandleResponse> getCandles(
            String symbol,
            String resolution,
            long startEpochSeconds,
            long endEpochSeconds
    );
}