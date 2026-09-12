package com.fintrade.fintrade.repository;

import com.fintrade.fintrade.entity.TradeOrder;
import com.fintrade.fintrade.entity.TradingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {

    List<TradeOrder> findAllByAccountOrderByCreatedAtDesc(TradingAccount account);
}
