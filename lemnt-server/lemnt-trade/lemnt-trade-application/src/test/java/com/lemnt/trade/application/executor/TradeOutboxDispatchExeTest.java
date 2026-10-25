package com.lemnt.trade.application.executor;

import com.lemnt.trade.domain.gateway.ProductCatalogGateway;
import com.lemnt.trade.domain.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.gateway.TradeOutboxGateway;
import com.lemnt.trade.domain.model.OutboxEvent;
import com.lemnt.trade.domain.model.TradeOrder;
import com.lemnt.trade.domain.model.TradeOrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TradeOutboxDispatchExeTest {
    TradeOutboxGateway outbox = mock(TradeOutboxGateway.class);
    TradeOrderGateway orders = mock(TradeOrderGateway.class);
    ProductCatalogGateway catalog = mock(ProductCatalogGateway.class);
    TradeOutboxDispatchExe dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new TradeOutboxDispatchExe(outbox, orders, catalog);
    }

    private TradeOrder order(String status) {
        return new TradeOrder(1L, "TR1", 3L, new BigDecimal("10.00"),
                status, List.of(new TradeOrderItem(5L, "P5", "test",
                    new BigDecimal("10.00"), 1, new BigDecimal("10.00"))), null);
    }

    @Test
    void confirmsStockForPaidOrder() {
        when(outbox.pending(5)).thenReturn(List.of(new OutboxEvent("E1", "TR1", "TRADE_ORDER_PAID", "{}", 0)));
        when(outbox.claim("E1")).thenReturn(true);
        when(orders.findByOrderNo("TR1")).thenReturn(order("PAID"));

        assertEquals(1, dispatcher.execute(5));
        verify(catalog).confirmStock("TR1", 5L);
        verify(outbox).delivered("E1");
    }

    @Test
    void releasesStockForClosedOrder() {
        when(outbox.pending(5)).thenReturn(List.of(new OutboxEvent("E2", "TR1", "TRADE_ORDER_CLOSED", "{}", 0)));
        when(outbox.claim("E2")).thenReturn(true);
        when(orders.findByOrderNo("TR1")).thenReturn(order("CLOSED"));

        assertEquals(1, dispatcher.execute(5));
        verify(catalog).releaseStock("TR1", 5L);
        verify(outbox).delivered("E2");
    }

    @Test
    void retainsFailedEventForRetry() {
        when(outbox.pending(5)).thenReturn(List.of(new OutboxEvent("E3", "TR1", "TRADE_ORDER_PAID", "{}", 0)));
        when(outbox.claim("E3")).thenReturn(true);
        when(orders.findByOrderNo("TR1")).thenReturn(order("PAID"));
        doThrow(new IllegalStateException("RPC down")).when(catalog).confirmStock("TR1", 5L);

        assertEquals(0, dispatcher.execute(5));
        verify(outbox).failed("E3");
        verify(outbox, never()).delivered("E3");
    }

    @Test
    void skipsEventClaimedByAnotherWorker() {
        when(outbox.pending(5)).thenReturn(List.of(new OutboxEvent("E4", "TR1", "TRADE_ORDER_PAID", "{}", 0)));
        when(outbox.claim("E4")).thenReturn(false);

        assertEquals(0, dispatcher.execute(5));
        verifyNoInteractions(catalog);
    }
}
