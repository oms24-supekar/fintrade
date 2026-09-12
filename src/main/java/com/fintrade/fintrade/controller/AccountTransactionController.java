package com.fintrade.fintrade.controller;

import com.fintrade.fintrade.dto.AccountTransactionRequest;
import com.fintrade.fintrade.dto.AccountTransactionResponse;
import com.fintrade.fintrade.service.AccountTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts/{accountId}/transactions")
public class AccountTransactionController {

    private final AccountTransactionService transactionService;

    public AccountTransactionController(AccountTransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountTransactionResponse> deposit(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.deposit(accountId, request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<AccountTransactionResponse> withdraw(
            @PathVariable Long accountId,
            @Valid @RequestBody AccountTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.withdraw(accountId, request));
    }

    @GetMapping
    public ResponseEntity<List<AccountTransactionResponse>> getHistory(
            @PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getHistory(accountId));
    }
}
