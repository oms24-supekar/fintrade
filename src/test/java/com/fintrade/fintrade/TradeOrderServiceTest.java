package com.fintrade.fintrade;

import com.fintrade.fintrade.dto.TradeOrderRequest;
import com.fintrade.fintrade.dto.TradeOrderResponse;
import com.fintrade.fintrade.entity.AccountTransaction;
import com.fintrade.fintrade.entity.Position;
import com.fintrade.fintrade.entity.TradeOrder;
import com.fintrade.fintrade.entity.TradeSide;
import com.fintrade.fintrade.entity.TradingAccount;
import com.fintrade.fintrade.entity.TransactionType;
import com.fintrade.fintrade.entity.User;
import com.fintrade.fintrade.repository.AccountTransactionRepository;
import com.fintrade.fintrade.repository.PositionRepository;
import com.fintrade.fintrade.repository.TradeOrderRepository;
import com.fintrade.fintrade.repository.TradingAccountRepository;
import com.fintrade.fintrade.repository.UserRepository;
import com.fintrade.fintrade.service.TradeOrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeOrderServiceTest {

    @Mock
    private TradeOrderRepository orderRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private AccountTransactionRepository transactionRepository;

    @Mock
    private TradingAccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    private TradeOrderService tradeOrderService;
    private User user;
    private TradingAccount account;

    @BeforeEach
    void setUp() {
        tradeOrderService = new TradeOrderService(
                orderRepository,
                positionRepository,
                transactionRepository,
                accountRepository,
                userRepository);
        user = new User();
        user.setUsername("alice");
        account = new TradingAccount();
        account.setUser(user);
        account.setAccountName("Primary");
        account.setBroker("Demo Broker");
        account.setCurrency("USD");
        account.setBalance(new BigDecimal("1000.0000"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", null, List.of()));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void buyOrderDebitsCashAndCreatesPositionAndLedgerEntry() {
        when(accountRepository.findByIdAndUserForUpdate(1L, user)).thenReturn(Optional.of(account));
        when(positionRepository.findByAccountAndSymbolForUpdate(account, "AAPL"))
                .thenReturn(Optional.empty());
        when(positionRepository.save(any(Position.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(AccountTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(accountRepository.save(any(TradingAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(TradeOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TradeOrderRequest request = new TradeOrderRequest();
        request.setSymbol("aapl");
        request.setSide(TradeSide.BUY);
        request.setQuantity(new BigDecimal("2"));
        request.setPrice(new BigDecimal("100"));

        TradeOrderResponse response = tradeOrderService.placeOrder(1L, request);

        assertThat(account.getBalance()).isEqualByComparingTo("800.0000");
        assertThat(response.getSymbol()).isEqualTo("AAPL");
        assertThat(response.getTotalValue()).isEqualByComparingTo("200.0000");

        ArgumentCaptor<Position> positionCaptor = ArgumentCaptor.forClass(Position.class);
        verify(positionRepository).save(positionCaptor.capture());
        assertThat(positionCaptor.getValue().getQuantity()).isEqualByComparingTo("2");
        assertThat(positionCaptor.getValue().getAveragePrice()).isEqualByComparingTo("100");

        ArgumentCaptor<AccountTransaction> transactionCaptor = ArgumentCaptor.forClass(AccountTransaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        assertThat(transactionCaptor.getValue().getType()).isEqualTo(TransactionType.TRADE_BUY);
        assertThat(transactionCaptor.getValue().getBalanceAfter()).isEqualByComparingTo("800.0000");
    }

    @Test
    void sellOrderRejectsQuantityAbovePosition() {
        when(accountRepository.findByIdAndUserForUpdate(1L, user)).thenReturn(Optional.of(account));
        Position position = new Position();
        position.setAccount(account);
        position.setSymbol("AAPL");
        position.setQuantity(new BigDecimal("1"));
        position.setAveragePrice(new BigDecimal("100"));
        when(positionRepository.findByAccountAndSymbolForUpdate(account, "AAPL"))
                .thenReturn(Optional.of(position));

        TradeOrderRequest request = new TradeOrderRequest();
        request.setSymbol("AAPL");
        request.setSide(TradeSide.SELL);
        request.setQuantity(new BigDecimal("2"));
        request.setPrice(new BigDecimal("100"));

        assertThatThrownBy(() -> tradeOrderService.placeOrder(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Insufficient position quantity");
        verify(accountRepository, never()).save(any(TradingAccount.class));
        verify(orderRepository, never()).save(any(TradeOrder.class));
        verify(transactionRepository, never()).save(any(AccountTransaction.class));
    }
}
