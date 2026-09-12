package com.fintrade.fintrade.repository;

import com.fintrade.fintrade.entity.Position;
import com.fintrade.fintrade.entity.TradingAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {

    List<Position> findAllByAccountOrderBySymbolAsc(TradingAccount account);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select position from Position position where position.account = :account and position.symbol = :symbol")
    Optional<Position> findByAccountAndSymbolForUpdate(
            @Param("account") TradingAccount account,
            @Param("symbol") String symbol);
}
