package com.lemnt.product.application.event;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.product.application.event.service.OrderStockEventHandler;
import com.lemnt.product.application.product.service.ProductInventoryService;
import com.lemnt.product.domain.event.gateway.MessageInboxGateway;
import com.lemnt.product.domain.order.gateway.OrderEventFactsGateway;
import com.lemnt.product.domain.order.model.OrderPaidFacts;
import com.lemnt.product.domain.order.model.OrderStockLine;
import com.lemnt.product.domain.product.gateway.ProductSkuGateway;
import com.lemnt.product.domain.product.gateway.ProductSpuGateway;
import com.lemnt.product.domain.product.model.ProductSku;
import com.lemnt.product.domain.product.model.ProductSpu;
import com.lemnt.product.domain.product.model.ProductStockReservation;

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
        when(inbox.begin(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);
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
        when(inventory.reservationStatus("ORDER", 10L)).thenReturn(reservation(10L, 2));
        when(inventory.reservationStatus("ORDER", 20L)).thenReturn(reservation(20L, 3));

        handler.paid(event);

        var locks = inOrder(spus, inventory);
        locks.verify(spus).lockSpu(5L);
        locks.verify(spus).lockSpu(8L);
        locks.verify(inventory).reservationStatus("ORDER", 10L);
        locks.verify(inventory).confirmStock("ORDER", 10L);
        locks.verify(inventory).reservationStatus("ORDER", 20L);
        locks.verify(inventory).confirmStock("ORDER", 20L);
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
        when(inventory.reservationStatus("ORDER", 10L)).thenReturn(reservation(10L, 2));
        when(inventory.reservationStatus("ORDER", 20L)).thenReturn(reservation(20L, 3));

        handler.paid(event);

        verify(spus).lockSpu(5L);
        verify(spus).addSales(5L, 5);
        verifyNoMoreInteractions(spus);
    }

    @Test
    void paid_whenLegacySkuHasNoSpu_shouldConfirmStockWithoutInventingSalesParent() {
        when(facts.paidFacts("payload"))
                .thenReturn(new OrderPaidFacts("ORDER", List.of(new OrderStockLine(10L, 2))));
        when(skus.catalogSku(10L)).thenReturn(sku(10L, null));
        when(inventory.reservationStatus("ORDER", 10L)).thenReturn(reservation(10L, 2));

        handler.paid(event);

        verify(inventory).confirmStock("ORDER", 10L);
        verifyNoInteractions(spus);
    }

    @Test
    void paid_whenSkuIsMissing_shouldFailInsteadOfSilentlyLosingSales() {
        when(facts.paidFacts("payload"))
                .thenReturn(new OrderPaidFacts("ORDER", List.of(new OrderStockLine(10L, 2))));

        assertThrows(IllegalStateException.class, () -> handler.paid(event));

        verifyNoInteractions(inventory, spus);
    }

    private ProductSku sku(Long id, Long spuId) {
        return new ProductSku(
                id, spuId, "SKU", "商品", BigDecimal.TEN, 10, null, Map.of(), "ON_SHELF");
    }

    private ProductSpu spu(Long id) {
        return new ProductSpu(id, "SPU", "商品", 1L, 1L, null, null, "ON_SHELF", List.of());
    }

    private ProductStockReservation reservation(Long skuId, int quantity) {
        return new ProductStockReservation("ORDER", skuId, quantity, "RESERVED");
    }
}
