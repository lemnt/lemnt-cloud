package com.lemnt.trade.application.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.payment.service.WalletService;
import com.lemnt.trade.domain.payment.gateway.WalletGateway;
import com.lemnt.trade.domain.payment.model.Wallet;
import com.lemnt.trade.domain.payment.model.WalletBizType;
import com.lemnt.trade.domain.payment.model.WalletTransaction;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 钱包入账/出账/冻结/幂等与调整规则。 */
class WalletServiceTest {
    final WalletGateway wallets = mock(WalletGateway.class);
    final WalletService service = new WalletService(wallets);

    private Wallet wallet(String balance, String freeze, String expense, String recharge) {
        return new Wallet(10L, 1L, new BigDecimal(balance), new BigDecimal(freeze),
                new BigDecimal(expense), new BigDecimal(recharge), null);
    }

    @Test
    void creditIncreasesBalanceAndRechargeTotal() {
        when(wallets.createIfAbsent(1L)).thenReturn(wallet("10.00", "0.00", "0.00", "0.00"));
        when(wallets.lockByMemberId(1L)).thenReturn(wallet("10.00", "0.00", "0.00", "0.00"));
        when(wallets.update(any())).thenReturn(true);
        var result = service.credit(1L, WalletBizType.RECHARGE, "R1", new BigDecimal("5.00"), "充值");
        assertEquals(new BigDecimal("15.00"), result.balance());
        assertEquals(new BigDecimal("5.00"), result.totalRecharge());
        ArgumentCaptor<WalletTransaction> captor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(wallets).insertTransaction(captor.capture());
        assertEquals(new BigDecimal("5.00"), captor.getValue().amount());
        assertEquals(new BigDecimal("15.00"), captor.getValue().balanceAfter());
        assertEquals("RECHARGE", captor.getValue().bizType());
    }

    @Test
    void debitRejectsInsufficientBalanceWithoutWriting() {
        when(wallets.createIfAbsent(1L)).thenReturn(wallet("1.00", "0.00", "0.00", "0.00"));
        when(wallets.lockByMemberId(1L)).thenReturn(wallet("1.00", "0.00", "0.00", "0.00"));
        assertThrows(ServiceException.class,
                () -> service.debit(1L, WalletBizType.PAYMENT, "P1", new BigDecimal("2.00"), "支付"));
        verify(wallets, never()).update(any());
        verify(wallets, never()).insertTransaction(any());
    }

    @Test
    void debitIncreasesExpenseAndWritesNegativeAmount() {
        when(wallets.createIfAbsent(1L)).thenReturn(wallet("10.00", "0.00", "0.00", "0.00"));
        when(wallets.lockByMemberId(1L)).thenReturn(wallet("10.00", "0.00", "0.00", "0.00"));
        when(wallets.update(any())).thenReturn(true);
        var result = service.debit(1L, WalletBizType.PAYMENT, "P1", new BigDecimal("4.00"), "支付");
        assertEquals(new BigDecimal("6.00"), result.balance());
        assertEquals(new BigDecimal("4.00"), result.totalExpense());
        ArgumentCaptor<WalletTransaction> captor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(wallets).insertTransaction(captor.capture());
        assertEquals(new BigDecimal("-4.00"), captor.getValue().amount());
    }

    @Test
    void freezeAndUnfreezeMoveBetweenAvailableAndFrozenWithoutLedger() {
        when(wallets.createIfAbsent(1L)).thenReturn(wallet("10.00", "0.00", "0.00", "0.00"));
        when(wallets.lockByMemberId(1L)).thenReturn(wallet("10.00", "0.00", "0.00", "0.00"),
                wallet("7.00", "3.00", "0.00", "0.00"));
        when(wallets.update(any())).thenReturn(true);
        var frozen = service.freeze(1L, new BigDecimal("3.00"));
        assertEquals(new BigDecimal("7.00"), frozen.balance());
        assertEquals(new BigDecimal("3.00"), frozen.freezePrice());
        var unfrozen = service.unfreeze(1L, new BigDecimal("2.00"));
        assertEquals(new BigDecimal("9.00"), unfrozen.balance());
        assertEquals(new BigDecimal("1.00"), unfrozen.freezePrice());
        verify(wallets, never()).insertTransaction(any());
    }

    @Test
    void sameBusinessIdIsIdempotentAndKeepsOriginalAmount() {
        when(wallets.createIfAbsent(1L)).thenReturn(wallet("15.00", "0.00", "0.00", "5.00"));
        when(wallets.lockByMemberId(1L)).thenReturn(wallet("15.00", "0.00", "0.00", "5.00"));
        when(wallets.findTransaction(10L, "RECHARGE", "R1")).thenReturn(new WalletTransaction(1L, "WT1", 10L,
                "RECHARGE", "R1", "充值", new BigDecimal("5.00"), new BigDecimal("15.00"), null));
        service.credit(1L, WalletBizType.RECHARGE, "R1", new BigDecimal("5.00"), "充值");
        verify(wallets, never()).update(any());
        verify(wallets, never()).insertTransaction(any());
        assertThrows(IllegalStateException.class,
                () -> service.credit(1L, WalletBizType.RECHARGE, "R1", new BigDecimal("6.00"), "充值"));
    }

    @Test
    void adjustUsesSignedAmountAndRejectsZeroOrPrecision() {
        when(wallets.createIfAbsent(1L)).thenReturn(wallet("10.00", "0.00", "0.00", "0.00"));
        when(wallets.lockByMemberId(1L)).thenReturn(wallet("10.00", "0.00", "0.00", "0.00"));
        when(wallets.update(any())).thenReturn(true);
        var result = service.adjust(1L, "A1", new BigDecimal("-2.00"), "扣减");
        assertEquals(new BigDecimal("8.00"), result.balance());
        assertEquals(new BigDecimal("2.00"), result.totalExpense());
        ArgumentCaptor<WalletTransaction> captor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(wallets).insertTransaction(captor.capture());
        assertEquals(new BigDecimal("-2.00"), captor.getValue().amount());
        assertEquals("UPDATE_BALANCE", captor.getValue().bizType());
        assertThrows(IllegalArgumentException.class,
                () -> service.adjust(1L, "A2", new BigDecimal("0.00"), "零"));
        assertThrows(IllegalArgumentException.class,
                () -> service.adjust(1L, "A3", new BigDecimal("1.005"), "精度"));
    }

    @Test
    void invalidMemberOrAmountIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.credit(0L, WalletBizType.RECHARGE, "R1", BigDecimal.ONE, "充值"));
        assertThrows(IllegalArgumentException.class,
                () -> service.credit(1L, WalletBizType.RECHARGE, "", BigDecimal.ONE, "充值"));
        assertThrows(IllegalArgumentException.class,
                () -> service.credit(1L, WalletBizType.RECHARGE, "R1", new BigDecimal("-1.00"), "充值"));
    }
}
