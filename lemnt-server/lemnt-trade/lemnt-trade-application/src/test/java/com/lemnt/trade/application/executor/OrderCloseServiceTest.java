package com.lemnt.trade.application.executor;

import com.lemnt.trade.application.order.service.OrderCloseService;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.payment.gateway.PaymentChannelCloseGateway;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.payment.model.PaymentRecord;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderCloseServiceTest {

    final OrderPersistenceService orderPersistenceService = mock(OrderPersistenceService.class);
    TradeOrderGateway tradeOrderGateway = mock(TradeOrderGateway.class);
    final TradePaymentGateway payments = mock(TradePaymentGateway.class);
    final PaymentChannelCloseGateway channels = mock(PaymentChannelCloseGateway.class);
    final OrderCloseService closer =
    new OrderCloseService(orderPersistenceService, tradeOrderGateway, payments, channels, mock(com.lemnt.trade.application.order.service.OrderLogService.class));

    PaymentRecord waiting() {
        return new PaymentRecord(
                1L, "PAY1", "TR1", "ALIPAY", null, BigDecimal.TEN, "WAITING", null);
    }

    void pending() {
        when(orderPersistenceService.findByOrderNoForUpdate("TR1"))
            .thenReturn(
                new TradeOrder(
                1L, "TR1", 2L, BigDecimal.TEN, "PENDING_PAYMENT", List.of(), null));
    }

    @Test
    void unknownOrPaidChannelDoesNotCloseOrReleaseStock() {
        pending();
        when(payments.findByOrderNo("TR1")).thenReturn(List.of(waiting()));
        assertFalse(closer.execute("TR1"));
        verify(tradeOrderGateway, never()).transitionStatus(anyString(), anyString(), anyString());
    }

    @Test
    void closesOnlyAfterProviderGuaranteesUnpaidIntentIsClosed() {
        pending();
        when(payments.findByOrderNo("TR1")).thenReturn(List.of(waiting()));
        when(channels.closeUnpaid(waiting())).thenReturn(true);
        when(payments.markClosed("PAY1")).thenReturn(true);
        when(tradeOrderGateway.transitionStatus("TR1", "PENDING_PAYMENT", "CLOSED")).thenReturn(true);
        assertTrue(closer.execute("TR1"));
        var sequence = inOrder(tradeOrderGateway, orderPersistenceService, channels, payments);
        sequence.verify(orderPersistenceService).findByOrderNoForUpdate("TR1");
        sequence.verify(payments).findByOrderNo("TR1");
        sequence.verify(channels).closeUnpaid(waiting());
        sequence.verify(payments).markClosed("PAY1");
        sequence.verify(tradeOrderGateway).transitionStatus("TR1", "PENDING_PAYMENT", "CLOSED");
    }

    @Test
    void providerFailureCannotCreateReleaseEvent() {
        pending();
        when(payments.findByOrderNo("TR1")).thenReturn(List.of(waiting()));
        when(channels.closeUnpaid(waiting())).thenThrow(new IllegalStateException("timeout"));
        assertThrows(IllegalStateException.class, () -> closer.execute("TR1"));
    }

    @Test
    void orderWithoutPaymentCanCloseUnderOrderLock() {
        pending();
        when(payments.findByOrderNo("TR1")).thenReturn(List.of());
        when(tradeOrderGateway.transitionStatus("TR1", "PENDING_PAYMENT", "CLOSED")).thenReturn(true);
        assertTrue(closer.execute("TR1"));
        verifyNoInteractions(channels);
    }
}
