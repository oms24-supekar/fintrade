package com.fintrade.fintrade.controller;

import com.fintrade.fintrade.dto.MarketQuoteResponse;
import com.fintrade.fintrade.exception.ResourceNotFoundException;
import com.fintrade.fintrade.service.MarketQuoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market/quotes")
public class MarketQuoteController {

    private final MarketQuoteService marketQuoteService;

    public MarketQuoteController(MarketQuoteService marketQuoteService) {
        this.marketQuoteService = marketQuoteService;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<MarketQuoteResponse> getQuote(@PathVariable String symbol) {
        MarketQuoteResponse quote = marketQuoteService.findQuote(symbol)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Market quote unavailable for symbol: " + symbol));
        return ResponseEntity.ok(quote);
    }
}
