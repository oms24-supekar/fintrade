package com.fintrade.fintrade.controller;

import com.fintrade.fintrade.dto.PositionResponse;
import com.fintrade.fintrade.dto.TradeOrderRequest;
import com.fintrade.fintrade.dto.TradeOrderResponse;
import com.fintrade.fintrade.service.TradeOrderService;
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
@RequestMapping("/api/accounts/{accountId}")
public class TradeOrderController {

    private final TradeOrderService tradeOrderService;

    public TradeOrderController(TradeOrderService tradeOrderService) {
        this.tradeOrderService = tradeOrderService;
    }

    @PostMapping("/orders")
    public ResponseEntity<TradeOrderResponse> placeOrder(
            @PathVariable Long accountId,
            @Valid @RequestBody TradeOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tradeOrderService.placeOrder(accountId, request));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<TradeOrderResponse>> getOrders(@PathVariable Long accountId) {
        return ResponseEntity.ok(tradeOrderService.getOrders(accountId));
    }

    @GetMapping("/positions")
    public ResponseEntity<List<PositionResponse>> getPositions(@PathVariable Long accountId) {
        return ResponseEntity.ok(tradeOrderService.getPositions(accountId));
    }
}
