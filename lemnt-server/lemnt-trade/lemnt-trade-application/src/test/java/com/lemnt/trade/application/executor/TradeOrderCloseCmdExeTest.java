package com.lemnt.trade.application.executor;

import com.lemnt.trade.domain.gateway.*;
import com.lemnt.trade.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TradeOrderCloseCmdExeTest {
    final TradeOrderGateway orders = mock(TradeOrderGateway.class);
    final TradeOutboxGateway outbox = mock(TradeOutboxGateway.class);
    final TradePaymentGateway payments = mock(TradePaymentGateway.class);
    final PaymentChannelCloseGateway channels = mock(PaymentChannelCloseGateway.class);
    final TradeOrderCloseCmdExe closer =
            new TradeOrderCloseCmdExe(
                    orders,
                    outbox,
                    payments,
                    channels,
                    mock(com.lemnt.trade.domain.checkout.gateway.CheckoutGateway.class));

    PaymentRecord waiting() {
        return new PaymentRecord(
                1L, "PAY1", "TR1", "ALIPAY", null, BigDecimal.TEN, "WAITING", null);
    }

    void pending() {
        when(orders.findByOrderNoForUpdate("TR1"))
                .thenReturn(
                        new TradeOrder(
                                1L, "TR1", 2L, BigDecimal.TEN, "PENDING_PAYMENT", List.of(), null));
    }

    @Test
    void unknownOrPaidChannelDoesNotCloseOrReleaseStock() {
        pending();
        when(payments.findByOrderNo("TR1")).thenReturn(List.of(waiting()));
        assertFalse(closer.execute("TR1"));
        verify(orders, never()).transitionStatus(anyString(), anyString(), anyString());
        verifyNoInteractions(outbox);
    }

    @Test
    void closesOnlyAfterProviderGuaranteesUnpaidIntentIsClosed() {
        pending();
        when(payments.findByOrderNo("TR1")).thenReturn(List.of(waiting()));
        when(channels.closeUnpaid(waiting())).thenReturn(true);
        when(payments.markClosed("PAY1")).thenReturn(true);
        when(orders.transitionStatus("TR1", "PENDING_PAYMENT", "CLOSED")).thenReturn(true);
        assertTrue(closer.execute("TR1"));
        var sequence = inOrder(orders, channels, payments, outbox);
        sequence.verify(orders).findByOrderNoForUpdate("TR1");
        sequence.verify(payments).findByOrderNo("TR1");
        sequence.verify(channels).closeUnpaid(waiting());
        sequence.verify(payments).markClosed("PAY1");
        sequence.verify(orders).transitionStatus("TR1", "PENDING_PAYMENT", "CLOSED");
        sequence.verify(outbox)
                .append(
                        anyString(),
                        eq("TRADE_ORDER"),
                        eq("TR1"),
                        eq("TRADE_ORDER_CLOSED"),
                        anyString());
    }

    @Test
    void providerFailureCannotCreateReleaseEvent() {
        pending();
        when(payments.findByOrderNo("TR1")).thenReturn(List.of(waiting()));
        when(channels.closeUnpaid(waiting())).thenThrow(new IllegalStateException("timeout"));
        assertThrows(IllegalStateException.class, () -> closer.execute("TR1"));
        verifyNoInteractions(outbox);
    }

    @Test
    void orderWithoutPaymentCanCloseUnderOrderLock() {
        pending();
        when(payments.findByOrderNo("TR1")).thenReturn(List.of());
        when(orders.transitionStatus("TR1", "PENDING_PAYMENT", "CLOSED")).thenReturn(true);
        assertTrue(closer.execute("TR1"));
        verifyNoInteractions(channels);
    }
}
