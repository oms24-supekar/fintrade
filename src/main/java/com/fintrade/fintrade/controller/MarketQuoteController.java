package com.fintrade.fintrade.controller;

import com.fintrade.fintrade.dto.MarketCandleResponse;
import com.fintrade.fintrade.dto.MarketQuoteResponse;
import com.fintrade.fintrade.exception.ResourceNotFoundException;
import com.fintrade.fintrade.service.MarketQuoteService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketQuoteController {

    private final MarketQuoteService marketQuoteService;

    public MarketQuoteController(
            MarketQuoteService marketQuoteService) {

        this.marketQuoteService =
                marketQuoteService;
    }

    @GetMapping("/quotes/{symbol}")
    public ResponseEntity<MarketQuoteResponse> getQuote(
            @PathVariable String symbol) {

        MarketQuoteResponse quote =
                marketQuoteService
                        .findQuote(symbol)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Market quote unavailable for symbol: "
                                                + symbol
                                )
                        );

        return ResponseEntity.ok(quote);
    }

    @GetMapping("/candles/{symbol}")
    public ResponseEntity<List<MarketCandleResponse>> getCandles(

            @PathVariable String symbol,

            @RequestParam String resolution,

            @RequestParam long start,

            @RequestParam long end) {

        return ResponseEntity.ok(
                marketQuoteService.getCandles(
                        symbol,
                        resolution,
                        start,
                        end
                )
        );
    }
}