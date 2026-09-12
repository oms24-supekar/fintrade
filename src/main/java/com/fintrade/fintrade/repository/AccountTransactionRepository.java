package com.fintrade.fintrade.repository;

import com.fintrade.fintrade.entity.AccountTransaction;
import com.fintrade.fintrade.entity.TradingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {

    List<AccountTransaction> findAllByAccountOrderByCreatedAtDesc(TradingAccount account);
}
