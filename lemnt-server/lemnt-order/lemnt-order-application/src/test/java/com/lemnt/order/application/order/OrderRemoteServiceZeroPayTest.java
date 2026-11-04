package com.lemnt.order.application.order;

import com.lemnt.order.application.order.service.OrderLifecycleService;
import com.lemnt.order.application.order.service.OrderPaidCompletionService;
import com.lemnt.order.application.order.service.OrderPersistenceService;
import com.lemnt.order.application.order.service.OrderRemoteService;
import com.lemnt.order.domain.inventory.gateway.InventoryGateway;
import com.lemnt.order.domain.order.gateway.OrderBenefitsGateway;
import com.lemnt.order.domain.order.gateway.OrderBenefitsProgressGateway;
import com.lemnt.order.domain.order.gateway.OrderRollbackGateway;
import com.lemnt.order.domain.order.gateway.PromotionResourceGateway;
import com.lemnt.order.domain.order.model.TradeOrder;
import com.lemnt.order.domain.order.model.TradeOrderItem;

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
    private final OrderBenefitsProgressGateway progress =
            Mockito.mock(OrderBenefitsProgressGateway.class);
    private final PromotionResourceGateway promotions =
            Mockito.mock(PromotionResourceGateway.class);
    private final OrderPaidCompletionService completion =
            Mockito.mock(OrderPaidCompletionService.class);
    private final OrderRemoteService service =
            new OrderRemoteService(
                    orders, inventory, benefits, lifecycle, progress, promotions, completion);

    private TradeOrder order(String status) {
        return new TradeOrder(
                7L,
                "TR0",
                1L,
                BigDecimal.ZERO,
                status,
                List.of(
                        new TradeOrderItem(
                                2L,
                                "SKU",
                                "商品",
                                new BigDecimal("10.00"),
                                1,
                                new BigDecimal("10.00"),
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                3L,
                                "SPU",
                                null,
                                null)),
                null,
                100L,
                0L,
                null);
    }

    @Test
    void zeroAmountOrderIsPaidAndInventoryConfirmedAfterReservation() {
        var calls = new AtomicInteger();
        Mockito.when(orders.findByOrderNo("TR0"))
                .thenAnswer(
                        invocation ->
                                calls.getAndIncrement() < 4
                                        ? order("PENDING_PAYMENT")
                                        : order("PAID"));
        Mockito.when(progress.paid("TR0")).thenReturn(true);
        Mockito.when(promotions.paidRecord("TR0")).thenReturn(null);
        service.reserve("TR0");
        Mockito.verify(lifecycle).zeroPay("TR0");
        Mockito.verify(progress).paid("TR0");
        Mockito.verify(inventory, Mockito.never())
                .confirmStock(Mockito.anyString(), Mockito.anyLong());

        Mockito.verify(completion).finish("TR0", null);
    }

    @Test
    void paidWithoutConsumerReceiptsCannotWriteInventoryCompletion() {
        Mockito.when(orders.findByOrderNo("TR0")).thenReturn(order("PAID"));
        Mockito.when(progress.paid("TR0")).thenReturn(false);
        service.paid("TR0");
        Mockito.verify(completion, Mockito.never()).finish(Mockito.anyString(), Mockito.any());
        Mockito.verify(inventory, Mockito.never())
                .confirmStock(Mockito.anyString(), Mockito.anyLong());
        Mockito.verify(lifecycle, Mockito.never()).inventoryConfirmed(Mockito.anyString());
    }
}
