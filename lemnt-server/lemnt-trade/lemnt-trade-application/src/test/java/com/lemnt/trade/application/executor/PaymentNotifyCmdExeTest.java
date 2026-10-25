package com.lemnt.trade.application.executor;

import com.lemnt.trade.domain.gateway.*;
import com.lemnt.trade.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentNotifyCmdExeTest {
    final TradePaymentGateway payments = mock(TradePaymentGateway.class);
    final TradeOrderGateway orders = mock(TradeOrderGateway.class);
    final TradeOutboxGateway outbox = mock(TradeOutboxGateway.class);
    final PaymentNotifyCmdExe notifier =
            new PaymentNotifyCmdExe(
                    payments,
                    orders,
                    outbox,
                    mock(com.lemnt.trade.domain.checkout.gateway.CheckoutGateway.class));
    final VerifiedPaymentNotification notice =
            new VerifiedPaymentNotification(
                    "PAY1", "TR1", "ALIPAY", "CHANNEL1", BigDecimal.TEN, "NOTICE2");

    @BeforeEach
    void setup() {
        when(orders.findByOrderNoForUpdate("TR1"))
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
        notifier.execute(notice);
        verify(payments, never()).markSuccess(any());
        verifyNoInteractions(outbox);
    }

    @Test
    void mismatchedAmountOrChannelTransactionIsRejected() {
        payment("WAITING", null, BigDecimal.ONE);
        assertThrows(IllegalArgumentException.class, () -> notifier.execute(notice));
        payment("SUCCESS", "OTHER", BigDecimal.TEN);
        assertThrows(IllegalStateException.class, () -> notifier.execute(notice));
        verifyNoInteractions(outbox);
    }

    @Test
    void successWritesPaymentOrderAndInventoryEventInOrder() {
        payment("WAITING", null, BigDecimal.TEN);
        when(payments.markSuccess(notice)).thenReturn(true);
        when(orders.transitionStatus("TR1", "PENDING_PAYMENT", "PAID")).thenReturn(true);
        notifier.execute(notice);
        var sequence = inOrder(orders, payments, outbox);
        sequence.verify(orders).findByOrderNoForUpdate("TR1");
        sequence.verify(payments).findByPaymentNo("PAY1");
        sequence.verify(payments).markSuccess(notice);
        sequence.verify(orders).transitionStatus("TR1", "PENDING_PAYMENT", "PAID");
        sequence.verify(outbox)
                .append(
                        anyString(),
                        eq("TRADE_ORDER"),
                        eq("TR1"),
                        eq("TRADE_ORDER_PAID"),
                        anyString());
    }

    @Test
    void failedStatusTransitionCannotAppendConfirmationEvent() {
        payment("WAITING", null, BigDecimal.TEN);
        when(payments.markSuccess(notice)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> notifier.execute(notice));
        verifyNoInteractions(outbox);
    }
}
