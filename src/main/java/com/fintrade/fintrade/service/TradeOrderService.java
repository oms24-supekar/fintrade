package com.fintrade.fintrade.service;

import com.fintrade.fintrade.dto.PositionResponse;
import com.fintrade.fintrade.dto.TradeOrderRequest;
import com.fintrade.fintrade.dto.TradeOrderResponse;
import com.fintrade.fintrade.entity.AccountStatus;
import com.fintrade.fintrade.entity.AccountTransaction;
import com.fintrade.fintrade.entity.OrderStatus;
import com.fintrade.fintrade.entity.Position;
import com.fintrade.fintrade.entity.TradeOrder;
import com.fintrade.fintrade.entity.TradeSide;
import com.fintrade.fintrade.entity.TradingAccount;
import com.fintrade.fintrade.entity.TransactionType;
import com.fintrade.fintrade.entity.User;
import com.fintrade.fintrade.exception.ResourceNotFoundException;
import com.fintrade.fintrade.repository.AccountTransactionRepository;
import com.fintrade.fintrade.repository.PositionRepository;
import com.fintrade.fintrade.repository.TradeOrderRepository;
import com.fintrade.fintrade.repository.TradingAccountRepository;
import com.fintrade.fintrade.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class TradeOrderService {

    private final TradeOrderRepository orderRepository;
    private final PositionRepository positionRepository;
    private final AccountTransactionRepository transactionRepository;
    private final TradingAccountRepository accountRepository;
    private final UserRepository userRepository;

    public TradeOrderService(
            TradeOrderRepository orderRepository,
            PositionRepository positionRepository,
            AccountTransactionRepository transactionRepository,
            TradingAccountRepository accountRepository,
            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.positionRepository = positionRepository;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TradeOrderResponse placeOrder(Long accountId, TradeOrderRequest request) {
        User user = getCurrentUser();
        TradingAccount account = accountRepository.findByIdAndUserForUpdate(accountId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trading account not found with id: " + accountId));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Orders are only allowed on active accounts");
        }

        String symbol = request.getSymbol().trim().toUpperCase();
        BigDecimal quantity = request.getQuantity();
        BigDecimal price = request.getPrice();
        BigDecimal totalValue = quantity.multiply(price).setScale(4, RoundingMode.HALF_UP);
        Position position = positionRepository.findByAccountAndSymbolForUpdate(account, symbol).orElse(null);

        if (request.getSide() == TradeSide.BUY) {
            if (account.getBalance().compareTo(totalValue) < 0) {
                throw new IllegalArgumentException("Insufficient account balance");
            }
            position = applyBuyPosition(account, position, symbol, quantity, price);
            account.setBalance(account.getBalance().subtract(totalValue));
            saveCashTransaction(account, TransactionType.TRADE_BUY, totalValue,
                    "Bought " + quantity + " " + symbol);
        } else {
            if (position == null || position.getQuantity().compareTo(quantity) < 0) {
                throw new IllegalArgumentException("Insufficient position quantity");
            }
            applySellPosition(position, quantity);
            account.setBalance(account.getBalance().add(totalValue));
            saveCashTransaction(account, TransactionType.TRADE_SELL, totalValue,
                    "Sold " + quantity + " " + symbol);
        }

        accountRepository.save(account);

        TradeOrder order = new TradeOrder();
        order.setAccount(account);
        order.setSymbol(symbol);
        order.setSide(request.getSide());
        order.setQuantity(quantity);
        order.setPrice(price);
        order.setTotalValue(totalValue);
        order.setStatus(OrderStatus.FILLED);

        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<TradeOrderResponse> getOrders(Long accountId) {
        TradingAccount account = findOwnedAccount(accountId);
        return orderRepository.findAllByAccountOrderByCreatedAtDesc(account)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PositionResponse> getPositions(Long accountId) {
        TradingAccount account = findOwnedAccount(accountId);
        return positionRepository.findAllByAccountOrderBySymbolAsc(account)
                .stream()
                .map(this::toPositionResponse)
                .toList();
    }

    private Position applyBuyPosition(
            TradingAccount account,
            Position position,
            String symbol,
            BigDecimal quantity,
            BigDecimal price) {
        if (position == null) {
            position = new Position();
            position.setAccount(account);
            position.setSymbol(symbol);
            position.setQuantity(quantity);
            position.setAveragePrice(price);
        } else {
            BigDecimal oldValue = position.getQuantity().multiply(position.getAveragePrice());
            BigDecimal newQuantity = position.getQuantity().add(quantity);
            BigDecimal newAveragePrice = oldValue.add(quantity.multiply(price))
                    .divide(newQuantity, 8, RoundingMode.HALF_UP);
            position.setQuantity(newQuantity);
            position.setAveragePrice(newAveragePrice);
        }
        return positionRepository.save(position);
    }

    private void applySellPosition(Position position, BigDecimal quantity) {
        BigDecimal remaining = position.getQuantity().subtract(quantity);
        if (remaining.signum() == 0) {
            positionRepository.delete(position);
        } else {
            position.setQuantity(remaining);
            positionRepository.save(position);
        }
    }

    private void saveCashTransaction(
            TradingAccount account,
            TransactionType type,
            BigDecimal amount,
            String description) {
        AccountTransaction transaction = new AccountTransaction();
        transaction.setAccount(account);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }

    private TradingAccount findOwnedAccount(Long accountId) {
        return accountRepository.findByIdAndUser(accountId, getCurrentUser())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trading account not found with id: " + accountId));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private TradeOrderResponse toResponse(TradeOrder order) {
        return new TradeOrderResponse(
                order.getId(),
                order.getSymbol(),
                order.getSide(),
                order.getQuantity(),
                order.getPrice(),
                order.getTotalValue(),
                order.getStatus(),
                order.getCreatedAt());
    }

    private PositionResponse toPositionResponse(Position position) {
        return new PositionResponse(
                position.getId(),
                position.getSymbol(),
                position.getQuantity(),
                position.getAveragePrice(),
                position.getUpdatedAt());
    }
}
