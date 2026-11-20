package com.lemnt.product.application.event;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.product.application.event.service.OrderStockEventHandler;
import com.lemnt.product.application.product.service.ProductInventoryService;
import com.lemnt.product.domain.event.gateway.MessageInboxGateway;
import com.lemnt.product.domain.order.gateway.OrderEventFactsGateway;
import com.lemnt.product.domain.order.model.OrderClosedFacts;
import com.lemnt.product.domain.order.model.OrderPaidFacts;
import com.lemnt.product.domain.order.model.OrderStockLine;
import com.lemnt.product.domain.product.gateway.ProductSkuGateway;
import com.lemnt.product.domain.product.gateway.ProductSpuGateway;
import com.lemnt.product.domain.product.model.ProductSku;
import com.lemnt.product.domain.product.model.ProductSpu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 销量写入必须沿用后台编辑的SPU→SKU锁序，避免新增支付确认与商品编辑之间的锁反转。
 */
@ExtendWith(MockitoExtension.class)
class OrderStockEventHandlerSalesTest {
    @Mock private MessageInboxGateway inbox;
    @Mock private ProductInventoryService inventory;
    @Mock private ProductSkuGateway skus;
    @Mock private ProductSpuGateway spus;
    @Mock private OrderEventFactsGateway facts;
    private OrderStockEventHandler handler;
    private CommerceEventEnvelope event;

    @BeforeEach
    void setup() {
        handler = new OrderStockEventHandler(inbox, inventory, skus, spus, facts);
        event =
                new CommerceEventEnvelope(
                        "E1",
                        CommerceEventTopics.ORDER_PAID,
                        1,
                        "ORDER",
                        "ORDER",
                        Instant.now(),
                        null,
                        "payload");
    }

    @Test
    void paid_whenMultipleSpus_shouldLockParentsInIdOrderBeforeAnySku() {
        when(facts.paidFacts("payload"))
                .thenReturn(
                        new OrderPaidFacts(
                                "ORDER",
                                List.of(new OrderStockLine(10L, 2), new OrderStockLine(20L, 3))));
        when(skus.catalogSku(10L)).thenReturn(sku(10L, 8L));
        when(skus.catalogSku(20L)).thenReturn(sku(20L, 5L));
        when(spus.lockSpu(5L)).thenReturn(spu(5L));
        when(spus.lockSpu(8L)).thenReturn(spu(8L));
        when(inventory.confirmSale("ORDER", 10L, 2)).thenReturn(true);
        when(inventory.confirmSale("ORDER", 20L, 3)).thenReturn(true);

        handler.paid(event);

        var locks = inOrder(spus, inventory);
        locks.verify(spus).lockSpu(5L);
        locks.verify(spus).lockSpu(8L);
        locks.verify(inventory).confirmSale("ORDER", 10L, 2);
        locks.verify(inventory).confirmSale("ORDER", 20L, 3);
        locks.verify(spus).addSales(5L, 3);
        locks.verify(spus).addSales(8L, 2);
    }

    @Test
    void paid_whenSkusShareSpu_shouldLockAndIncrementParentOnce() {
        when(facts.paidFacts("payload"))
                .thenReturn(
                        new OrderPaidFacts(
                                "ORDER",
                                List.of(new OrderStockLine(10L, 2), new OrderStockLine(20L, 3))));
        when(skus.catalogSku(10L)).thenReturn(sku(10L, 5L));
        when(skus.catalogSku(20L)).thenReturn(sku(20L, 5L));
        when(spus.lockSpu(5L)).thenReturn(spu(5L));
        when(inventory.confirmSale("ORDER", 10L, 2)).thenReturn(true, false);
        when(inventory.confirmSale("ORDER", 20L, 3)).thenReturn(true, false);

        handler.paid(event);
        handler.paid(event);

        verify(spus, times(2)).lockSpu(5L);
        verify(spus).addSales(5L, 5);
        verify(skus).addSales(10L, 2);
        verify(skus).addSales(20L, 3);
        verify(inbox, never())
                .begin(anyString(), anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(spus);
    }

    @Test
    void paid_whenLegacySkuHasNoSpu_shouldConfirmStockWithoutInventingSalesParent() {
        when(facts.paidFacts("payload"))
                .thenReturn(new OrderPaidFacts("ORDER", List.of(new OrderStockLine(10L, 2))));
        when(skus.catalogSku(10L)).thenReturn(sku(10L, null));
        when(inventory.confirmSale("ORDER", 10L, 2)).thenReturn(true);

        handler.paid(event);

        verify(inventory).confirmSale("ORDER", 10L, 2);
        verifyNoInteractions(spus);
    }

    @Test
    void paid_whenSkuIsMissing_shouldFailInsteadOfSilentlyLosingSales() {
        when(facts.paidFacts("payload"))
                .thenReturn(new OrderPaidFacts("ORDER", List.of(new OrderStockLine(10L, 2))));

        assertThrows(IllegalStateException.class, () -> handler.paid(event));

        verifyNoInteractions(inventory, spus);
    }

    @Test
    void closeVerifiesOrderStateOwnerAndAllQuantitiesBeforeReleasing() {
        var closed =
                new CommerceEventEnvelope(
                        "C1",
                        CommerceEventTopics.ORDER_CLOSED,
                        1,
                        "ORDER",
                        "ORDER",
                        Instant.now(),
                        null,
                        "payload");
        var first = new OrderStockLine(10L, 2);
        var second = new OrderStockLine(20L, 3);
        when(facts.closedFacts("payload"))
                .thenReturn(new OrderClosedFacts("ORDER", 1L, List.of(second, first)));
        when(facts.findClosedOrder("ORDER"))
                .thenReturn(
                        null,
                        new OrderClosedFacts("ORDER", 2L, List.of(first, second)),
                        new OrderClosedFacts("ORDER", 1L, List.of(first)),
                        new OrderClosedFacts("ORDER", 1L, List.of(first, second)));
        assertThrows(IllegalStateException.class, () -> handler.closed(closed));
        assertThrows(IllegalStateException.class, () -> handler.closed(closed));
        assertThrows(IllegalStateException.class, () -> handler.closed(closed));
        verifyNoInteractions(inventory);
        handler.closed(closed);
        var sequence = inOrder(inventory);
        sequence.verify(inventory).cancelReservation("ORDER", 10L, 2);
        sequence.verify(inventory).cancelReservation("ORDER", 20L, 3);
        verify(inbox, never())
                .begin(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void closeLegacyReplayVerifiesTheOldPayloadWithoutCallingOrderOrTouchingStock() {
        var closed =
                new CommerceEventEnvelope(
                        "C1",
                        CommerceEventTopics.ORDER_CLOSED,
                        1,
                        "ORDER",
                        "ORDER",
                        Instant.now(),
                        null,
                        "payload");
        when(facts.closedFacts("payload"))
                .thenReturn(new OrderClosedFacts("ORDER", 1L, List.of(new OrderStockLine(10L, 2))));
        when(inbox.has("product.order_closed.v1", CommerceEventTopics.ORDER_CLOSED, "ORDER"))
                .thenReturn(true);
        handler.closed(closed);
        verify(inbox).begin("product.order_closed.v1", "C1", "ORDER_CLOSED", "ORDER", "payload");
        verify(facts, never()).findClosedOrder(anyString());
        verifyNoInteractions(inventory);
    }

    private ProductSku sku(Long id, Long spuId) {
        return new ProductSku(
                id, spuId, "SKU", "商品", BigDecimal.TEN, 10, null, Map.of(), "ON_SHELF");
    }

    private ProductSpu spu(Long id) {
        return new ProductSpu(id, "SPU", "商品", 1L, 1L, null, null, "ON_SHELF", List.of());
    }
}
