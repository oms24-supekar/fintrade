package com.fintrade.fintrade.service;

import com.fintrade.fintrade.dto.PortfolioSummaryResponse;
import com.fintrade.fintrade.dto.PositionResponse;
import com.fintrade.fintrade.dto.MarketQuoteResponse;
import com.fintrade.fintrade.entity.Position;
import com.fintrade.fintrade.entity.TradingAccount;
import com.fintrade.fintrade.entity.User;
import com.fintrade.fintrade.exception.ResourceNotFoundException;
import com.fintrade.fintrade.repository.PositionRepository;
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
public class PortfolioService {

    private final TradingAccountRepository accountRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
        private final MarketQuoteService marketQuoteService;

    public PortfolioService(
            TradingAccountRepository accountRepository,
            PositionRepository positionRepository,
            UserRepository userRepository,
            MarketQuoteService marketQuoteService) {
        this.accountRepository = accountRepository;
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
        this.marketQuoteService = marketQuoteService;
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getSummary(Long accountId) {
        TradingAccount account = accountRepository.findByIdAndUser(accountId, getCurrentUser())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trading account not found with id: " + accountId));

        List<Position> positions = positionRepository.findAllByAccountOrderBySymbolAsc(account);
        List<PositionResponse> positionResponses = positions.stream()
                .map(this::toResponse)
                .toList();
        BigDecimal positionCostBasis = positions.stream()
                .map(position -> position.getQuantity().multiply(position.getAveragePrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);
                BigDecimal positionMarketValue = BigDecimal.ZERO;
                int quotesAvailable = 0;
                for (Position position : positions) {
                        MarketQuoteResponse quote = marketQuoteService.findQuote(position.getSymbol()).orElse(null);
                        BigDecimal valuationPrice = position.getAveragePrice();
                        if (quote != null) {
                                valuationPrice = quote.getPrice();
                                quotesAvailable++;
                        }
                        positionMarketValue = positionMarketValue.add(position.getQuantity().multiply(valuationPrice));
                }
                positionMarketValue = positionMarketValue.setScale(4, RoundingMode.HALF_UP);
                BigDecimal totalEquity = account.getBalance().add(positionMarketValue);

        return new PortfolioSummaryResponse(
                account.getId(),
                account.getCurrency(),
                account.getBalance(),
                positionCostBasis,
                positionMarketValue,
                totalEquity,
                quotesAvailable,
                positionResponses);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private PositionResponse toResponse(Position position) {
        return new PositionResponse(
                position.getId(),
                position.getSymbol(),
                position.getQuantity(),
                position.getAveragePrice(),
                position.getUpdatedAt());
    }
}
