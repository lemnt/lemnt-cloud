package com.lemnt.trade.application.executor;

import com.lemnt.trade.application.order.service.OrderLifecycleService;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.application.outbox.executor.TradeOutboxDispatchCmdExe;
import com.lemnt.trade.domain.inventory.gateway.InventoryGateway;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.order.model.TradeOrderItem;
import com.lemnt.trade.domain.outbox.gateway.TradeOutboxGateway;
import com.lemnt.trade.domain.outbox.model.OutboxEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TradeOutboxDispatchExeTest {
    TradeOutboxGateway outbox = mock(TradeOutboxGateway.class);
    OrderPersistenceService orderPersistenceService = mock(OrderPersistenceService.class);
    InventoryGateway catalog = mock(InventoryGateway.class);
    TradeOutboxDispatchCmdExe dispatcher;
    OrderLifecycleService lifecycle =
    mock(OrderLifecycleService.class);

    @BeforeEach
    void setUp() {
        dispatcher = new TradeOutboxDispatchCmdExe(orderPersistenceService, outbox, catalog, mock(com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway.class), lifecycle);
    }

    private TradeOrder order(String status) {
        return new TradeOrder(
                1L,
                "TR1",
                3L,
                new BigDecimal("10.00"),
                status,
                List.of(
                new TradeOrderItem(
                5L,
                "P5",
                "test",
                new BigDecimal("10.00"),
                1,
                new BigDecimal("10.00"))),
                null);
    }

    @Test
    void confirmsStockForPaidOrder() {
        when(outbox.pending(5))
            .thenReturn(List.of(new OutboxEvent("E1", "TR1", "TRADE_ORDER_PAID", "{}", 0)));
        when(outbox.claim(eq("E1"), anyString())).thenReturn(true);
        when(orderPersistenceService.findByOrderNo("TR1")).thenReturn(order("PAID"));

        when(outbox.delivered(anyString(), anyString())).thenReturn(true);
        assertEquals(1, dispatcher.execute(5));
        var sequence = inOrder(catalog, lifecycle);
        sequence.verify(catalog).confirmStock("TR1", 5L);
        sequence.verify(lifecycle).inventoryConfirmed("TR1");
        verify(outbox).delivered(eq("E1"), anyString());
    }

    @Test
    void releasesStockForClosedOrder() {
        when(outbox.pending(5))
            .thenReturn(List.of(new OutboxEvent("E2", "TR1", "TRADE_ORDER_CLOSED", "{}", 0)));
        when(outbox.claim(eq("E2"), anyString())).thenReturn(true);
        when(orderPersistenceService.findByOrderNo("TR1")).thenReturn(order("CLOSED"));

        when(outbox.delivered(anyString(), anyString())).thenReturn(true);
        assertEquals(1, dispatcher.execute(5));
        verify(catalog).cancelReservation("TR1", 5L, 1);
        verify(outbox).delivered(eq("E2"), anyString());
    }

    @Test
    void retainsFailedEventForRetry() {
        when(outbox.pending(5))
            .thenReturn(List.of(new OutboxEvent("E3", "TR1", "TRADE_ORDER_PAID", "{}", 0)));
        when(outbox.claim(eq("E3"), anyString())).thenReturn(true);
        when(orderPersistenceService.findByOrderNo("TR1")).thenReturn(order("PAID"));
        doThrow(new IllegalStateException("RPC down")).when(catalog).confirmStock("TR1", 5L);

        assertEquals(0, dispatcher.execute(5));
        verify(outbox).failed(eq("E3"), anyString(), eq(1));
        verify(outbox, never()).delivered(eq("E3"), anyString());
        verify(lifecycle, never()).inventoryConfirmed(anyString());
    }

    @Test
    void partiallyCompletedBatchRetriesEveryItemUsingStableReservationKey() {
        var items =
        List.of(
                new TradeOrderItem(5L, "P5", "first", BigDecimal.TEN, 1, BigDecimal.TEN),
                new TradeOrderItem(6L, "P6", "second", BigDecimal.TEN, 1, BigDecimal.TEN));
        when(outbox.pending(5))
            .thenReturn(List.of(new OutboxEvent("E6", "TR1", "TRADE_ORDER_PAID", "{}", 0)));
        when(outbox.claim(eq("E6"), anyString())).thenReturn(true);
        when(orderPersistenceService.findByOrderNo("TR1"))
            .thenReturn(
                new TradeOrder(
                1L, "TR1", 3L, new BigDecimal("20.00"), "PAID", items, null));
        doThrow(new IllegalStateException("RPC down"))
            .doNothing()
            .when(catalog)
            .confirmStock("TR1", 6L);
        when(outbox.delivered(eq("E6"), anyString())).thenReturn(true);
        assertEquals(0, dispatcher.execute(5));
        assertEquals(1, dispatcher.execute(5));
        verify(catalog, times(2)).confirmStock("TR1", 5L);
        verify(catalog, times(2)).confirmStock("TR1", 6L);
    }

    @Test
    void expiredOwnerDoesNotCountDelivery() {
        when(outbox.pending(5))
            .thenReturn(List.of(new OutboxEvent("E5", "TR1", "TRADE_ORDER_PAID", "{}", 0)));
        when(outbox.claim(eq("E5"), anyString())).thenReturn(true);
        when(orderPersistenceService.findByOrderNo("TR1")).thenReturn(order("PAID"));
        when(outbox.delivered(eq("E5"), anyString())).thenReturn(false);
        assertEquals(0, dispatcher.execute(5));
        verify(outbox, never()).failed(anyString(), anyString(), anyInt());
    }

    @Test
    void skipsEventClaimedByAnotherWorker() {
        when(outbox.pending(5))
            .thenReturn(List.of(new OutboxEvent("E4", "TR1", "TRADE_ORDER_PAID", "{}", 0)));
        when(outbox.claim(eq("E4"), anyString())).thenReturn(false);

        assertEquals(0, dispatcher.execute(5));
        verifyNoInteractions(catalog);
    }
}
