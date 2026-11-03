package com.lemnt.order.start.fulfillment;

import com.lemnt.order.application.fulfillment.service.OrderFulfillmentService;
import com.lemnt.order.domain.fulfillment.gateway.ShipmentGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.model.TradeOperator;
import com.lemnt.order.start.TradeOrderAutoReceiveScheduler;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/** 自动收货只处理超期物流单，单条异常不影响其余订单。 */
class AutoReceiveSchedulerTest {
    final ShipmentGateway shipments = mock(ShipmentGateway.class);
    final TradeOrderGateway orders = mock(TradeOrderGateway.class);
    final OrderFulfillmentService fulfillment = mock(OrderFulfillmentService.class);

    @Test
    void receivesExpiredShipmentsAndIsolatesFailures() {
        when(orders.databaseNow()).thenReturn(LocalDateTime.of(2026, 11, 1, 12, 0));
        when(shipments.shippedBefore(any(), eq(50))).thenReturn(List.of("TR1", "TR2"));
        doThrow(new IllegalStateException("conflict"))
                .when(fulfillment)
                .receive("TR1", TradeOperator.system());

        new TradeOrderAutoReceiveScheduler(shipments, orders, fulfillment, Duration.ofDays(7))
                .execute();

        verify(fulfillment).receive("TR1", TradeOperator.system());
        verify(fulfillment).receive("TR2", TradeOperator.system());
    }

    @Test
    void rejectsNonPositiveExpiry() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new TradeOrderAutoReceiveScheduler(
                                shipments, orders, fulfillment, Duration.ZERO));
    }
}
