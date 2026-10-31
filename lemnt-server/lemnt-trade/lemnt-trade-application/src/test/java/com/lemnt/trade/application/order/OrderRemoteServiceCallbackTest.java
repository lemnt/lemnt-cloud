package com.lemnt.trade.application.order;

import com.lemnt.trade.application.order.service.OrderLifecycleService;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.application.order.service.OrderRemoteService;
import com.lemnt.trade.domain.inventory.gateway.InventoryGateway;
import com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway;
import com.lemnt.trade.domain.order.gateway.OrderRollbackGateway;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.order.model.TradeOrderItem;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** 商城同步预占回调失败时必须关闭订单并发布统一补偿，避免订单占用 SKU 库存。 */
class OrderRemoteServiceCallbackTest {
    private final OrderPersistenceService orders = Mockito.mock(OrderPersistenceService.class);
    private final InventoryGateway inventory = Mockito.mock(InventoryGateway.class);
    private final OrderBenefitsGateway benefits = Mockito.mock(OrderBenefitsGateway.class);
    private final OrderLifecycleService lifecycle = Mockito.mock(OrderLifecycleService.class);
    private final OrderRollbackGateway rollback = Mockito.mock(OrderRollbackGateway.class);
    private final OrderRemoteService service =
            new OrderRemoteService(orders, inventory, benefits, lifecycle, rollback);

    private TradeOrder order(String status) {
        return new TradeOrder(7L, "TR1", 1L, new BigDecimal("10.00"), status,
                List.of(new TradeOrderItem(2L, "SKU", "商品", new BigDecimal("10.00"), 1,
                        new BigDecimal("10.00"), new BigDecimal("10.00"), BigDecimal.ZERO, 3L, "SPU",
                        null, null)), null, 0L, 0L, null);
    }

    @Test
    void mallReservedFailureClosesOrderAndPublishesRollback() {
        var calls = new AtomicInteger();
        Mockito.when(orders.findByOrderNo("TR1")).thenAnswer(invocation ->
                calls.getAndIncrement() < 3 ? order("PENDING_PAYMENT") : order("CLOSED"));
        Mockito.doThrow(new IllegalStateException("活动库存不足")).when(benefits).reserved("TR1");
        assertThrows(IllegalStateException.class, () -> service.reserve("TR1"));
        Mockito.verify(lifecycle).reservationCallbackFailed("TR1");
        Mockito.verify(rollback).publish("TR1");
    }
}
