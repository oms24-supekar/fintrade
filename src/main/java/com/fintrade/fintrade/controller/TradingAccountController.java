package com.fintrade.fintrade.controller;

import com.fintrade.fintrade.dto.TradingAccountRequest;
import com.fintrade.fintrade.dto.TradingAccountResponse;
import com.fintrade.fintrade.service.TradingAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class TradingAccountController {

    private final TradingAccountService tradingAccountService;

    public TradingAccountController(TradingAccountService tradingAccountService) {
        this.tradingAccountService = tradingAccountService;
    }

    @PostMapping
    public ResponseEntity<TradingAccountResponse> createAccount(
            @Valid @RequestBody TradingAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tradingAccountService.createAccount(request));
    }

    @GetMapping
    public ResponseEntity<List<TradingAccountResponse>> getCurrentUserAccounts() {
        return ResponseEntity.ok(tradingAccountService.getCurrentUserAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TradingAccountResponse> getCurrentUserAccount(@PathVariable Long id) {
        return ResponseEntity.ok(tradingAccountService.getCurrentUserAccount(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCurrentUserAccount(@PathVariable Long id) {
        tradingAccountService.deleteCurrentUserAccount(id);
        return ResponseEntity.noContent().build();
    }
}
