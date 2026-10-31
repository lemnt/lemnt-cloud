package com.lemnt.trade.application.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.payment.service.RechargeService;
import com.lemnt.trade.application.payment.service.WalletService;
import com.lemnt.trade.domain.payment.gateway.RechargePackageGateway;
import com.lemnt.trade.domain.payment.gateway.WalletRechargeGateway;
import com.lemnt.trade.domain.payment.model.RechargePackage;
import com.lemnt.trade.domain.payment.model.Wallet;
import com.lemnt.trade.domain.payment.model.WalletBizType;
import com.lemnt.trade.domain.payment.model.WalletRecharge;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 充值下单幂等、到账入账、退款出账与状态约束。 */
class RechargeServiceTest {
    final RechargePackageGateway packages = mock(RechargePackageGateway.class);
    final WalletRechargeGateway recharges = mock(WalletRechargeGateway.class);
    final WalletService walletService = mock(WalletService.class);
    final RechargeService service = new RechargeService(packages, recharges, walletService);
    final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 0, 0);

    private RechargePackage pkg(int status) {
        return new RechargePackage(1L, "标准", new BigDecimal("100.00"), new BigDecimal("20.00"), status, null);
    }

    private WalletRecharge recharge(String status) {
        return new WalletRecharge(5L, "RC1", "REQ1", 10L, 1L, 1L, new BigDecimal("100.00"),
                new BigDecimal("20.00"), new BigDecimal("120.00"), status, "ALIPAY", now, null, null);
    }

    private Wallet wallet() {
        return new Wallet(10L, 1L, new BigDecimal("120.00"), new BigDecimal("0.00"), new BigDecimal("0.00"),
                new BigDecimal("120.00"), null);
    }

    @Test
    void createSnapshotsPackageAndIsIdempotentByRequest() {
        when(packages.find(1L)).thenReturn(pkg(0));
        when(recharges.findByRequestNo("REQ1")).thenReturn(null);
        when(walletService.getOrCreate(1L)).thenReturn(wallet());
        when(recharges.createIfAbsent(any())).thenReturn(recharge("PENDING"));
        var value = service.create(1L, 1L, "REQ1");
        assertEquals("PENDING", value.status());
        ArgumentCaptor<WalletRecharge> captor = ArgumentCaptor.forClass(WalletRecharge.class);
        verify(recharges).createIfAbsent(captor.capture());
        assertEquals(new BigDecimal("120.00"), captor.getValue().totalPrice());
        assertEquals(10L, captor.getValue().walletId());

        when(recharges.findByRequestNo("REQ1")).thenReturn(recharge("PENDING"));
        assertEquals(5L, service.create(1L, 1L, "REQ1").id());
        verify(recharges, times(1)).createIfAbsent(any());
    }

    @Test
    void createRejectsDisabledPackageAndReusedRequestForDifferentContent() {
        when(packages.find(1L)).thenReturn(pkg(1));
        assertThrows(ServiceException.class, () -> service.create(1L, 1L, "REQ1"));
        when(packages.find(1L)).thenReturn(pkg(0));
        when(recharges.findByRequestNo("REQ1")).thenReturn(
                new WalletRecharge(5L, "RC1", "REQ1", 10L, 1L, 2L, BigDecimal.ONE, BigDecimal.ZERO,
                        BigDecimal.ONE, "PENDING", null, null, null, null));
        assertThrows(IllegalStateException.class, () -> service.create(1L, 1L, "REQ1"));
    }

    @Test
    void confirmPaidCreditsWalletAndRepeatsAreIdempotent() {
        when(recharges.lockByNo("RC1")).thenReturn(recharge("PENDING"));
        when(recharges.databaseNow()).thenReturn(now);
        when(recharges.update(any())).thenReturn(true);
        when(walletService.credit(anyLong(), any(), anyString(), any(), anyString())).thenReturn(wallet());
        when(recharges.findByNo("RC1")).thenReturn(recharge("PAID"));
        assertEquals("PAID", service.confirmPaid("RC1", "ALIPAY").status());
        verify(walletService).credit(1L, WalletBizType.RECHARGE, "RC1", new BigDecimal("120.00"), "充值到账");

        reset(walletService);
        when(recharges.lockByNo("RC1")).thenReturn(recharge("PAID"));
        assertEquals("PAID", service.confirmPaid("RC1", "ALIPAY").status());
        verifyNoInteractions(walletService);
    }

    @Test
    void refundDebitsWalletAndRejectsPendingOrRepeats() {
        when(recharges.lockByNo("RC1")).thenReturn(recharge("PAID"));
        when(recharges.databaseNow()).thenReturn(now);
        when(recharges.update(any())).thenReturn(true);
        when(walletService.debit(anyLong(), any(), anyString(), any(), anyString())).thenReturn(wallet());
        when(recharges.findByNo("RC1")).thenReturn(recharge("REFUNDED"));
        assertEquals("REFUNDED", service.refund("RC1").status());
        verify(walletService).debit(1L, WalletBizType.RECHARGE_REFUND, "RC1", new BigDecimal("120.00"), "充值退款");

        reset(walletService);
        when(recharges.lockByNo("RC1")).thenReturn(recharge("REFUNDED"));
        assertEquals("REFUNDED", service.refund("RC1").status());
        verifyNoInteractions(walletService);

        when(recharges.lockByNo("RC1")).thenReturn(recharge("PENDING"));
        assertThrows(ServiceException.class, () -> service.refund("RC1"));
    }

    @Test
    void refundPropagatesInsufficientBalance() {
        when(recharges.lockByNo("RC1")).thenReturn(recharge("PAID"));
        when(recharges.databaseNow()).thenReturn(now);
        when(recharges.update(any())).thenReturn(true);
        doThrow(new ServiceException("钱包余额不足", 400)).when(walletService)
                .debit(anyLong(), any(), anyString(), any(), anyString());
        assertThrows(ServiceException.class, () -> service.refund("RC1"));
    }

    @Test
    void missingRechargeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.confirmPaid("RC9", null));
        assertThrows(IllegalArgumentException.class, () -> service.refund("RC9"));
        verifyNoInteractions(walletService);
    }
}
