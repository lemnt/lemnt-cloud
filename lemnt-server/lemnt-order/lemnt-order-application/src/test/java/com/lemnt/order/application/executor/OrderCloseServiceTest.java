package com.lemnt.order.application.executor;

import com.lemnt.order.application.order.service.OrderCloseService;
import com.lemnt.order.application.order.service.OrderLogService;
import com.lemnt.order.application.order.service.OrderPersistenceService;
import com.lemnt.order.application.promotion.service.ActivityOrderLifecycleService;
import com.lemnt.order.domain.event.gateway.OrderEventGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.model.TradeOrder;
import com.lemnt.order.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.order.domain.payment.model.PaymentRecord;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 关单原约束由订单本地事实和支付消费共同保证；订单不能直接改支付库。 */
class OrderCloseServiceTest {
    final OrderPersistenceService persistence = mock(OrderPersistenceService.class);
    final TradeOrderGateway orders = mock(TradeOrderGateway.class);
    final TradePaymentGateway payments = mock(TradePaymentGateway.class);
    final OrderEventGateway events = mock(OrderEventGateway.class);
    final ActivityOrderLifecycleService activities = mock(ActivityOrderLifecycleService.class);
    final OrderCloseService closer =
            new OrderCloseService(
                    persistence, orders, payments, events, activities, mock(OrderLogService.class));

    void pending() {
        when(persistence.findByOrderNoForUpdate("TR1"))
                .thenReturn(
                        new TradeOrder(
                                1L, "TR1", 2L, BigDecimal.TEN, "PENDING_PAYMENT", List.of(), null));
    }

    PaymentRecord payment(String state) {
        return new PaymentRecord(
                1L, "PAY1", "TR1", "ALIPAY", null, null, BigDecimal.TEN, state, null);
    }

    @Test
    void unknownOrPaidChannelDoesNotCloseOrReleaseStock() {
        pending();
        when(payments.findByOrderNo("TR1")).thenReturn(List.of(payment("SUCCESS")));
        assertFalse(closer.execute("TR1"));
        verifyNoInteractions(events);
        verify(orders, never()).transitionStatus(any(), any(), any());
    }

    @Test
    void localClosePublishesReliableProviderCloseRequest() {
        pending();
        when(payments.findByOrderNo("TR1")).thenReturn(List.of(payment("WAITING")));
        when(orders.transitionStatus("TR1", "PENDING_PAYMENT", "CLOSED")).thenReturn(true);
        assertTrue(closer.execute("TR1"));
        var seq = inOrder(persistence, payments, orders, events);
        seq.verify(persistence).findByOrderNoForUpdate("TR1");
        seq.verify(payments).findByOrderNo("TR1");
        seq.verify(orders).transitionStatus("TR1", "PENDING_PAYMENT", "CLOSED");
        seq.verify(events).closed(any());
    }

    @Test
    void providerReadFailureCannotCreateReleaseEvent() {
        pending();
        when(payments.findByOrderNo("TR1")).thenThrow(new IllegalStateException("timeout"));
        assertThrows(IllegalStateException.class, () -> closer.execute("TR1"));
        verifyNoInteractions(events);
    }

    @Test
    void orderWithoutPaymentCanCloseUnderOrderLock() {
        pending();
        when(payments.findByOrderNo("TR1")).thenReturn(List.of());
        when(orders.transitionStatus("TR1", "PENDING_PAYMENT", "CLOSED")).thenReturn(true);
        assertTrue(closer.execute("TR1"));
        verify(events).closed(any());
    }
}
