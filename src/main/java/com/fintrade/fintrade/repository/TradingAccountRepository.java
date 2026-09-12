package com.fintrade.fintrade.repository;

import com.fintrade.fintrade.entity.TradingAccount;
import com.fintrade.fintrade.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TradingAccountRepository extends JpaRepository<TradingAccount, Long> {

    List<TradingAccount> findAllByUserOrderByCreatedAtDesc(User user);

    Optional<TradingAccount> findByIdAndUser(Long id, User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from TradingAccount account where account.id = :id and account.user = :user")
    Optional<TradingAccount> findByIdAndUserForUpdate(
            @Param("id") Long id,
            @Param("user") User user);
}
