package com.fintrade.fintrade.controller;

import com.fintrade.fintrade.dto.PortfolioSummaryResponse;
import com.fintrade.fintrade.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts/{accountId}/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public ResponseEntity<PortfolioSummaryResponse> getSummary(@PathVariable Long accountId) {
        return ResponseEntity.ok(portfolioService.getSummary(accountId));
    }
}
