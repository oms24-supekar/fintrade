package com.fintrade.fintrade.service;

import com.fintrade.fintrade.dto.AccountTransactionRequest;
import com.fintrade.fintrade.dto.AccountTransactionResponse;
import com.fintrade.fintrade.entity.AccountStatus;
import com.fintrade.fintrade.entity.AccountTransaction;
import com.fintrade.fintrade.entity.TradingAccount;
import com.fintrade.fintrade.entity.TransactionType;
import com.fintrade.fintrade.entity.User;
import com.fintrade.fintrade.exception.ResourceNotFoundException;
import com.fintrade.fintrade.repository.AccountTransactionRepository;
import com.fintrade.fintrade.repository.TradingAccountRepository;
import com.fintrade.fintrade.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountTransactionService {

    private final AccountTransactionRepository transactionRepository;
    private final TradingAccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountTransactionService(
            AccountTransactionRepository transactionRepository,
            TradingAccountRepository accountRepository,
            UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AccountTransactionResponse deposit(Long accountId, AccountTransactionRequest request) {
        return applyTransaction(accountId, request, TransactionType.DEPOSIT);
    }

    @Transactional
    public AccountTransactionResponse withdraw(Long accountId, AccountTransactionRequest request) {
        return applyTransaction(accountId, request, TransactionType.WITHDRAWAL);
    }

    @Transactional(readOnly = true)
    public List<AccountTransactionResponse> getHistory(Long accountId) {
        TradingAccount account = findOwnedAccount(accountId);
        return transactionRepository.findAllByAccountOrderByCreatedAtDesc(account)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AccountTransactionResponse applyTransaction(
            Long accountId,
            AccountTransactionRequest request,
            TransactionType type) {
        TradingAccount account = accountRepository.findByIdAndUserForUpdate(accountId, getCurrentUser())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Trading account not found with id: " + accountId));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Transactions are only allowed on active accounts");
        }

        BigDecimal amount = request.getAmount();
        BigDecimal currentBalance = account.getBalance();
        BigDecimal newBalance = type == TransactionType.DEPOSIT
                ? currentBalance.add(amount)
                : currentBalance.subtract(amount);

        if (newBalance.signum() < 0) {
            throw new IllegalArgumentException("Insufficient account balance");
        }

        account.setBalance(newBalance);
        accountRepository.save(account);

        AccountTransaction transaction = new AccountTransaction();
        transaction.setAccount(account);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(newBalance);
        transaction.setDescription(normalizeDescription(request.getDescription()));

        return toResponse(transactionRepository.save(transaction));
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

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String normalized = description.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private AccountTransactionResponse toResponse(AccountTransaction transaction) {
        return new AccountTransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getCreatedAt());
    }
}
