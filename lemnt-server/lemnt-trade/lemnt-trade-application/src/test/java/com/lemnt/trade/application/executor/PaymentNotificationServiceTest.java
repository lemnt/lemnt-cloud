package com.lemnt.trade.application.executor;

import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.application.payment.service.PaymentNotificationService;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.payment.model.PaymentRecord;
import com.lemnt.trade.domain.payment.model.VerifiedPaymentNotification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentNotificationServiceTest {

    final TradePaymentGateway payments = mock(TradePaymentGateway.class);
    final OrderPersistenceService orderPersistenceService = mock(OrderPersistenceService.class);
    TradeOrderGateway tradeOrderGateway = mock(TradeOrderGateway.class);
    final PaymentNotificationService notifier =
    new PaymentNotificationService(orderPersistenceService, tradeOrderGateway, payments, mock(com.lemnt.trade.application.order.service.OrderLogService.class));
    final VerifiedPaymentNotification notice =
    new VerifiedPaymentNotification(
            "PAY1", "TR1", "ALIPAY", "CHANNEL1", BigDecimal.TEN, "NOTICE2");

    @BeforeEach
    void setup() {
        when(orderPersistenceService.findByOrderNoForUpdate("TR1"))
            .thenReturn(
                new TradeOrder(
                1L, "TR1", 2L, BigDecimal.TEN, "PENDING_PAYMENT", List.of(), null));
    }

    void payment(String status, String tradeNo, BigDecimal amount) {
        when(payments.findByPaymentNo("PAY1"))
            .thenReturn(
                new PaymentRecord(
                1L, "PAY1", "TR1", "ALIPAY", tradeNo, amount, status, "NOTICE1"));
    }

    @Test
    void sameSuccessfulChannelTransactionIsIdempotentWithNewNotificationId() {
        payment("SUCCESS", "CHANNEL1", BigDecimal.TEN);
        orderStatus("PAID");
        notifier.execute(notice);
        verify(payments, never()).markSuccess(any());
    }

    @Test
    void mismatchedAmountOrChannelTransactionIsRejected() {
        payment("WAITING", null, BigDecimal.ONE);
        assertThrows(IllegalArgumentException.class, () -> notifier.execute(notice));
        payment("SUCCESS", "OTHER", BigDecimal.TEN);
        assertThrows(IllegalStateException.class, () -> notifier.execute(notice));
    }

    @Test
    void successWritesPaymentAndOrderInOrder() {
        payment("WAITING", null, BigDecimal.TEN);
        when(payments.markSuccess(notice)).thenReturn(true);
        when(tradeOrderGateway.transitionStatus("TR1", "PENDING_PAYMENT", "PAID")).thenReturn(true);
        notifier.execute(notice);
        var sequence = inOrder(tradeOrderGateway, orderPersistenceService, payments);
        sequence.verify(orderPersistenceService).findByOrderNoForUpdate("TR1");
        sequence.verify(payments).findByPaymentNo("PAY1");
        sequence.verify(payments).markSuccess(notice);
        sequence.verify(tradeOrderGateway).transitionStatus("TR1", "PENDING_PAYMENT", "PAID");
    }

    @Test
    void failedStatusTransitionCannotAppendConfirmationEvent() {
        payment("WAITING", null, BigDecimal.TEN);
        when(payments.markSuccess(notice)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> notifier.execute(notice));
    }

    void orderStatus(String status) {
        when(orderPersistenceService.findByOrderNoForUpdate("TR1"))
            .thenReturn(new TradeOrder(1L, "TR1", 2L, BigDecimal.TEN, status, List.of(), null));
    }

    @Test
    void successfulReplayRejectsInconsistentOrderStatesWithoutSideEffects() {
        payment("SUCCESS", "CHANNEL1", BigDecimal.TEN);
        for (String status : List.of("RESERVING", "PENDING_PAYMENT", "CLOSED")) {
            orderStatus(status);
            assertThrows(IllegalStateException.class, () -> notifier.execute(notice));
        }
        verify(payments, never()).markSuccess(any());
        verify(tradeOrderGateway, never()).transitionStatus(anyString(), anyString(), anyString());
    }

    @Test
    void successfulReplayRemainsIdempotentAfterShippingAndReceipt() {
        payment("SUCCESS", "CHANNEL1", BigDecimal.TEN);
        for (String status : List.of("SHIPPED", "COMPLETED")) {
            orderStatus(status);
            notifier.execute(notice);
        }
        verify(payments, never()).markSuccess(any());
    }
}
