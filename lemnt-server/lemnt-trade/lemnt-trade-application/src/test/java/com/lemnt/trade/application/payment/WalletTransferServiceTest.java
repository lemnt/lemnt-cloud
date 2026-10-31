package com.lemnt.trade.application.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.payment.service.WalletService;
import com.lemnt.trade.application.payment.service.WalletTransferService;
import com.lemnt.trade.domain.payment.gateway.WalletGateway;
import com.lemnt.trade.domain.payment.gateway.WalletTransferGateway;
import com.lemnt.trade.domain.payment.model.Wallet;
import com.lemnt.trade.domain.payment.model.WalletBizType;
import com.lemnt.trade.domain.payment.model.WalletTransfer;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 转账校验、幂等、双钱包行锁顺序与双向流水。 */
class WalletTransferServiceTest {
    final WalletGateway wallets = mock(WalletGateway.class);
    final WalletTransferGateway transfers = mock(WalletTransferGateway.class);
    final WalletService walletService = mock(WalletService.class);
    final WalletTransferService service = new WalletTransferService(wallets, transfers, walletService);

    private Wallet wallet(Long id, Long memberId) {
        return new Wallet(id, memberId, new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, null);
    }

    private WalletTransfer transfer(Long fromMember, Long toMember, String amount) {
        return new WalletTransfer(5L, "TF1", "REQ1", 20L, fromMember, 10L, toMember, new BigDecimal(amount),
                "备注", "SUCCESS", null);
    }

    private void stubCreateReturnsRecord() {
        when(transfers.createIfAbsent(any())).thenAnswer(invocation -> {
            WalletTransfer value = invocation.getArgument(0);
            return new WalletTransfer(5L, value.transferNo(), value.requestNo(), value.fromWalletId(),
                    value.fromMemberId(), value.toWalletId(), value.toMemberId(), value.amount(), value.remark(),
                    value.status(), null);
        });
    }

    @Test
    void transferLocksWalletsAscendingAndWritesBothSides() {
        when(wallets.findByMemberId(2L)).thenReturn(wallet(20L, 2L));
        when(walletService.getOrCreate(1L)).thenReturn(wallet(10L, 1L));
        when(transfers.findByRequestNo("REQ1")).thenReturn(null);
        stubCreateReturnsRecord();
        var result = service.transfer(1L, 2L, new BigDecimal("10.00"), "备注", "REQ1");
        assertEquals(5L, result.id());
        assertEquals("SUCCESS", result.status());
        InOrder order = inOrder(wallets);
        order.verify(wallets).lockById(10L);
        order.verify(wallets).lockById(20L);
        verify(walletService).debit(1L, WalletBizType.TRANSFER, "REQ1", new BigDecimal("10.00"), "转账转出");
        verify(walletService).credit(2L, WalletBizType.TRANSFER, "REQ1", new BigDecimal("10.00"), "转账转入");
    }

    @Test
    void transferLocksSmallerWalletFirstRegardlessOfDirection() {
        when(wallets.findByMemberId(2L)).thenReturn(wallet(10L, 2L));
        when(walletService.getOrCreate(1L)).thenReturn(wallet(20L, 1L));
        when(transfers.findByRequestNo("REQ1")).thenReturn(null);
        stubCreateReturnsRecord();
        service.transfer(1L, 2L, new BigDecimal("10.00"), null, "REQ1");
        InOrder order = inOrder(wallets);
        order.verify(wallets).lockById(10L);
        order.verify(wallets).lockById(20L);
    }

    @Test
    void repeatedRequestReturnsOriginalWithoutMovingMoneyAgain() {
        when(transfers.findByRequestNo("REQ1")).thenReturn(transfer(1L, 2L, "10.00"));
        var result = service.transfer(1L, 2L, new BigDecimal("10.00"), "备注", "REQ1");
        assertEquals(5L, result.id());
        verifyNoInteractions(walletService);
        verify(wallets, never()).lockById(anyLong());
    }

    @Test
    void reusedRequestWithDifferentContentIsRejected() {
        when(transfers.findByRequestNo("REQ1")).thenReturn(transfer(1L, 3L, "10.00"));
        assertThrows(IllegalStateException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("10.00"), null, "REQ1"));
        verifyNoInteractions(walletService);
    }

    @Test
    void selfTransferMissingReceiverAndBadAmountAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1L, 1L, new BigDecimal("10.00"), null, "REQ1"));
        when(transfers.findByRequestNo("REQ1")).thenReturn(null);
        when(wallets.findByMemberId(2L)).thenReturn(null);
        assertThrows(ServiceException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("10.00"), null, "REQ1"));
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("0.00"), null, "REQ1"));
        assertThrows(IllegalArgumentException.class,
                () -> service.transfer(1L, 2L, new BigDecimal("10.00"), null, ""));
    }
}
