package com.lemnt.trade.application.aftersale;

import com.lemnt.trade.application.aftersale.cmd.AfterSaleCreateCmd;
import com.lemnt.trade.application.aftersale.service.AfterSaleLogService;
import com.lemnt.trade.application.aftersale.service.AfterSaleService;
import com.lemnt.trade.application.order.service.OrderLogService;
import com.lemnt.trade.application.paymentrefund.service.PaymentRefundService;
import com.lemnt.trade.application.paymentrefund.service.RefundBenefitsService;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.aftersale.model.AfterSale;
import com.lemnt.trade.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.trade.domain.order.model.OrderRefundItem;
import com.lemnt.trade.domain.order.model.TradeOperator;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.trade.application.order.service.OrderPersistenceService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** 0元积分兑换订单售后：无支付单、零金额，售后完成后由权益任务退回积分。 */
class ZeroCashAfterSaleTest {
    private final TradeOrderGateway orders = Mockito.mock(TradeOrderGateway.class);
    private final TradeOrderItemGateway items = Mockito.mock(TradeOrderItemGateway.class);
    private final AfterSaleGateway afterSales = Mockito.mock(AfterSaleGateway.class);
    private final TradePaymentGateway payments = Mockito.mock(TradePaymentGateway.class);
    private final AfterSaleLogService logs = Mockito.mock(AfterSaleLogService.class);
    private final AfterSaleService service = new AfterSaleService(orders, items, afterSales, payments, logs);

    private TradeOrder order() {
        return new TradeOrder(7L, "TR0", 1L, BigDecimal.ZERO, "PAID", List.of(),
                LocalDateTime.now(), 100L, 0L, null);
    }

    private OrderRefundItem item() {
        return new OrderRefundItem(11L, 2L, "商品", 2, new BigDecimal("10.00"),
                BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2));
    }

    private AfterSale afterSale() {
        return new AfterSale("RF1", "TR0", "REQ", 1L, null, 11L, 2, "REFUND_ONLY",
                BigDecimal.ZERO.setScale(2), "原因", "WAIT_REFUND", null, null, null,
                LocalDateTime.now(), null, null, null, 5L, 7L, null, null, 2L, 3L, "商品",
                null, null);
    }

    @Test
    void zeroCashAfterSaleIsCreatedWithoutPaymentRecord() {
        Mockito.when(orders.lockHeader("TR0")).thenReturn(order());
        Mockito.when(afterSales.findRequest("TR0", "REQ")).thenReturn(null);
        Mockito.when(afterSales.afterSales("TR0")).thenReturn(List.of());
        Mockito.when(items.refundItems(7L)).thenReturn(List.of(item()));
        var saved = new AtomicReference<AfterSale>();
        Mockito.doAnswer(invocation -> {
            saved.set(invocation.getArgument(0));
            return null;
        }).when(afterSales).saveAfterSale(Mockito.any());
        Mockito.when(afterSales.afterSale(Mockito.anyString())).thenAnswer(invocation -> saved.get());
        var value = service.create(new AfterSaleCreateCmd("TR0", "REQ", 11L, 1, "REFUND_ONLY", "原因"),
                TradeOperator.system());
        assertEquals(BigDecimal.ZERO.setScale(2), value.amount());
        assertNull(value.paymentNo());
        assertEquals("REQUESTED", value.status());
    }

    @Test
    void zeroCashRefundCreatesSuccessfulPointRefundAndMarksOrderRefunded() {
        var afterSale = afterSale();
        Mockito.when(afterSales.afterSale("RF1")).thenReturn(afterSale);
        Mockito.when(orders.lockHeader("TR0")).thenReturn(order());
        Mockito.when(afterSales.lockAfterSale("RF1")).thenReturn(afterSale);
        Mockito.when(refunds().findByAfterSaleNo("RF1")).thenReturn(null);
        Mockito.when(inventory().inventoryConfirmed("TR0")).thenReturn(true);
        Mockito.when(afterSales.startPaymentRefund("RF1")).thenReturn(true);
        Mockito.when(afterSales.completePaymentRefund(Mockito.eq("RF1"), Mockito.any())).thenReturn(true);
        Mockito.when(items.refundItems(7L)).thenReturn(List.of(item()));
        Mockito.when(afterSales.afterSales("TR0")).thenReturn(List.of(new AfterSale("RF1", "TR0", "REQ",
                1L, null, 11L, 2, "REFUND_ONLY", BigDecimal.ZERO.setScale(2), "原因", "REFUNDED",
                null, null, null, LocalDateTime.now(), null, null, null, 5L, 7L, LocalDateTime.now(),
                null, 2L, 3L, "商品", null, null)));
        Mockito.when(orders.transitionStatus("TR0", "PAID", "REFUNDED")).thenReturn(true);
        var value = refundService().prepare("RF1", TradeOperator.system());
        assertEquals("SUCCESS", value.status());
        assertNull(value.paymentNo());
        assertEquals(0, BigDecimal.ZERO.compareTo(value.amount()));
        assertEquals("POINT", value.channel());
    }

    private PaymentRefundGateway refunds() {
        return refundGateway;
    }

    private InventoryConfirmationGateway inventory() {
        return inventoryConfirmation;
    }

    private final PaymentRefundGateway refundGateway = Mockito.mock(PaymentRefundGateway.class);
    private final InventoryConfirmationGateway inventoryConfirmation =
            Mockito.mock(InventoryConfirmationGateway.class);

    private PaymentRefundService refundService() {
        return new PaymentRefundService(orders, afterSales, payments, items, refundGateway, inventoryConfirmation,
                Mockito.mock(OrderLogService.class), Mockito.mock(AfterSaleLogService.class));
    }

    @Test
    void zeroRefundDispatchesBenefitsWithZeroAmount() {
        var afterSale = new AfterSale("RF1", "TR0", "REQ", 1L, null, 11L, 2, "REFUND_ONLY",
                BigDecimal.ZERO.setScale(2), "原因", "REFUNDED", null, null, null,
                LocalDateTime.now(), null, null, null, 5L, 7L, LocalDateTime.now(), null, 2L, 3L,
                "商品", null, null);
        var afterSales = Mockito.mock(AfterSaleGateway.class);
        var orderPersistence = Mockito.mock(OrderPersistenceService.class);
        var itemGateway = Mockito.mock(TradeOrderItemGateway.class);
        var benefits = Mockito.mock(OrderBenefitsGateway.class);
        Mockito.when(afterSales.afterSale("RF1")).thenReturn(afterSale);
        Mockito.when(orderPersistence.findByOrderNo("TR0")).thenReturn(order());
        Mockito.when(itemGateway.refundItems(7L)).thenReturn(List.of(item()));
        new RefundBenefitsService(afterSales, orderPersistence, itemGateway, benefits)
                .complete(new PaymentRefund("PR1", "RF1", null, "TR0", "POINT",
                        BigDecimal.ZERO.setScale(2), "SUCCESS", "POINTS"));
        Mockito.verify(benefits).refunded(Mockito.eq("TR0"), Mockito.eq("RF1"),
                Mockito.argThat(map -> Integer.valueOf(2).equals(map.get(2L))),
                Mockito.eq(BigDecimal.ZERO.setScale(2)));
    }
}
