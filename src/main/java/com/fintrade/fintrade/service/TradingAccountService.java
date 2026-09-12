package com.fintrade.fintrade.service;

import com.fintrade.fintrade.dto.TradingAccountRequest;
import com.fintrade.fintrade.dto.TradingAccountResponse;
import com.fintrade.fintrade.entity.TradingAccount;
import com.fintrade.fintrade.entity.User;
import com.fintrade.fintrade.exception.ResourceNotFoundException;
import com.fintrade.fintrade.repository.TradingAccountRepository;
import com.fintrade.fintrade.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TradingAccountService {

    private final TradingAccountRepository tradingAccountRepository;
    private final UserRepository userRepository;

    public TradingAccountService(
            TradingAccountRepository tradingAccountRepository,
            UserRepository userRepository) {
        this.tradingAccountRepository = tradingAccountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TradingAccountResponse createAccount(TradingAccountRequest request) {
        User user = getCurrentUser();

        TradingAccount account = new TradingAccount();
        account.setUser(user);
        account.setAccountName(request.getAccountName().trim());
        account.setBroker(request.getBroker().trim());
        account.setCurrency(request.getCurrency().trim().toUpperCase());
        account.setBalance(request.getInitialBalance() == null
                ? BigDecimal.ZERO
                : request.getInitialBalance());

        return toResponse(tradingAccountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public List<TradingAccountResponse> getCurrentUserAccounts() {
        User user = getCurrentUser();
        return tradingAccountRepository.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TradingAccountResponse getCurrentUserAccount(Long id) {
        return toResponse(findOwnedAccount(id));
    }

    @Transactional
    public void deleteCurrentUserAccount(Long id) {
        tradingAccountRepository.delete(findOwnedAccount(id));
    }

    private TradingAccount findOwnedAccount(Long id) {
        return tradingAccountRepository.findByIdAndUser(id, getCurrentUser())
                .orElseThrow(() -> new ResourceNotFoundException("Trading account not found with id: " + id));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private TradingAccountResponse toResponse(TradingAccount account) {
        return new TradingAccountResponse(
                account.getId(),
                account.getAccountName(),
                account.getBroker(),
                account.getCurrency(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }
}
