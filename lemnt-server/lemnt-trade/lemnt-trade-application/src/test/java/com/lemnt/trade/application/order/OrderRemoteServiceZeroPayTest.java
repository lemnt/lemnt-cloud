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

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/** 零元积分兑换在预占成功后直接进入已支付并确认库存。 */
class OrderRemoteServiceZeroPayTest {
    private final OrderPersistenceService orders = Mockito.mock(OrderPersistenceService.class);
    private final InventoryGateway inventory = Mockito.mock(InventoryGateway.class);
    private final OrderBenefitsGateway benefits = Mockito.mock(OrderBenefitsGateway.class);
    private final OrderLifecycleService lifecycle = Mockito.mock(OrderLifecycleService.class);
    private final OrderRollbackGateway rollback = Mockito.mock(OrderRollbackGateway.class);
    private final OrderRemoteService service =
            new OrderRemoteService(orders, inventory, benefits, lifecycle, rollback);

    private TradeOrder order(String status) {
        return new TradeOrder(7L, "TR0", 1L, BigDecimal.ZERO, status,
                List.of(new TradeOrderItem(2L, "SKU", "商品", new BigDecimal("10.00"), 1,
                        new BigDecimal("10.00"), BigDecimal.ZERO, BigDecimal.ZERO, 3L, "SPU",
                        null, null)), null, 100L, 0L, null);
    }

    @Test
    void zeroAmountOrderIsPaidAndInventoryConfirmedAfterReservation() {
        var calls = new AtomicInteger();
        Mockito.when(orders.findByOrderNo("TR0")).thenAnswer(invocation ->
                calls.getAndIncrement() < 4 ? order("PENDING_PAYMENT") : order("PAID"));
        service.reserve("TR0");
        Mockito.verify(lifecycle).zeroPay("TR0");
        Mockito.verify(inventory).confirmStock("TR0", 2L);
        Mockito.verify(benefits).paid("TR0");
        Mockito.verify(lifecycle).inventoryConfirmed("TR0");
    }
}
