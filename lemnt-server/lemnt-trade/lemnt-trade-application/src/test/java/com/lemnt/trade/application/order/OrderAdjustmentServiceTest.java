package com.lemnt.trade.application.order;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.order.cmd.OrderAdjustAddressCmd;
import com.lemnt.trade.application.order.cmd.OrderAdjustPriceCmd;
import com.lemnt.trade.application.order.service.OrderAdjustmentService;
import com.lemnt.trade.application.order.service.OrderLogService;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.order.model.TradeOrderDelivery;
import com.lemnt.trade.domain.order.model.TradeOrderItem;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 后台调价按原行实付分摊，改址只更新未发货快递单。 */
class OrderAdjustmentServiceTest {
    final TradeOrderGateway orders = mock(TradeOrderGateway.class);
    final TradeOrderItemGateway items = mock(TradeOrderItemGateway.class);
    final OrderPersistenceService persistence = mock(OrderPersistenceService.class);
    final OrderLogService logs = mock(OrderLogService.class);
    final OrderAdjustmentService service = new OrderAdjustmentService(orders, items, persistence, logs);

    TradeOrder order(String status, TradeOrderDelivery delivery) {
        return new TradeOrder(1L, "TR1", 7L, new BigDecimal("100.00"), status, List.of(), null,
                0L, 0L, delivery);
    }

    TradeOrderItem line(long id, String pay) {
        return new TradeOrderItem(5L, "SKU", "商品", BigDecimal.TEN, 1, BigDecimal.TEN,
                new BigDecimal(pay), BigDecimal.ZERO, 3L, "SPU", null, null, id, null);
    }

    @Test
    void priceIsAllocatedToItemPayAmounts() {
        var current = order("PENDING_PAYMENT", null);
        when(persistence.findByOrderNoForUpdate("TR1")).thenReturn(current);
        when(items.items(1L)).thenReturn(List.of(line(11L, "60.00"), line(12L, "40.00")));
        when(orders.updateTotalAmount("TR1", new BigDecimal("100.00"), new BigDecimal("80.00"))).thenReturn(true);
        when(items.updatePayAmount(1L, 11L, new BigDecimal("48.00"))).thenReturn(true);
        when(items.updatePayAmount(1L, 12L, new BigDecimal("32.00"))).thenReturn(true);
        when(persistence.findByOrderNo("TR1")).thenReturn(current);

        var result = service.adjustPrice(new OrderAdjustPriceCmd("TR1", new BigDecimal("80.00"), 9L));

        assertSame(current, result);
        verify(items).updatePayAmount(1L, 11L, new BigDecimal("48.00"));
        verify(items).updatePayAmount(1L, 12L, new BigDecimal("32.00"));
        verify(logs).record(any(), eq("PENDING_PAYMENT"), eq("PENDING_PAYMENT"), eq(2), any(), contains("80.00"));
    }

    @Test
    void paidOrderCannotBeRepriced() {
        when(persistence.findByOrderNoForUpdate("TR1")).thenReturn(order("PAID", null));
        assertThrows(ServiceException.class,
                () -> service.adjustPrice(new OrderAdjustPriceCmd("TR1", new BigDecimal("80.00"), 9L)));
    }

    @Test
    void addressUpdatesOnlyUnshippedExpressOrders() {
        var delivery = new TradeOrderDelivery(10L, 1, "张三", "13800000000", 110101L, "旧地址",
                BigDecimal.ZERO, null, null);
        var current = order("PAID", delivery);
        when(persistence.findByOrderNoForUpdate("TR1")).thenReturn(current);
        when(orders.updateAddress("TR1", "李四", "13900000000", 110102L, "新地址")).thenReturn(true);
        when(persistence.findByOrderNo("TR1")).thenReturn(current);

        service.adjustAddress(new OrderAdjustAddressCmd("TR1", "李四", "13900000000", 110102L, "新地址", 9L));

        verify(orders).updateAddress("TR1", "李四", "13900000000", 110102L, "新地址");
        verify(logs).record(any(), eq("PAID"), eq("PAID"), eq(11), any(), contains("李四"));
    }

    @Test
    void pickupOrderCannotChangeAddress() {
        var delivery = new TradeOrderDelivery(null, 2, null, null, null, null,
                BigDecimal.ZERO, 9L, "12345678");
        when(persistence.findByOrderNoForUpdate("TR1")).thenReturn(order("PAID", delivery));
        assertThrows(ServiceException.class, () -> service.adjustAddress(
                new OrderAdjustAddressCmd("TR1", "李四", "13900000000", 110102L, "新地址", 9L)));
    }
}
