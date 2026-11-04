package com.lemnt.order.application.aftersale;

import com.lemnt.order.application.aftersale.cmd.AfterSaleCreateCmd;
import com.lemnt.order.application.aftersale.service.AfterSaleLogService;
import com.lemnt.order.application.aftersale.service.AfterSalePromotionService;
import com.lemnt.order.application.aftersale.service.AfterSaleRefundCoordinator;
import com.lemnt.order.application.aftersale.service.AfterSaleService;
import com.lemnt.order.application.brokerage.service.BrokerageService;
import com.lemnt.order.application.order.service.OrderLogService;
import com.lemnt.order.application.paymentrefund.executor.PaymentRefundCreateCmdExe;
import com.lemnt.order.application.paymentrefund.service.RefundBenefitsService;
import com.lemnt.order.application.promotion.service.ActivityOrderLifecycleService;
import com.lemnt.order.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.order.domain.aftersale.model.AfterSale;
import com.lemnt.order.domain.event.gateway.OrderEventGateway;
import com.lemnt.order.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.order.domain.order.model.OrderRefundItem;
import com.lemnt.order.domain.order.model.TradeOperator;
import com.lemnt.order.domain.order.model.TradeOrder;
import com.lemnt.order.domain.order.model.TradeOrderItem;
import com.lemnt.order.domain.payment.gateway.PaymentGateway;
import com.lemnt.order.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.order.domain.paymentrefund.gateway.PaymentRefundGateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 保留六个原零现金场景，使用冻结退单和订单事件验证，不伪造渠道退款。 */
class ZeroCashAfterSaleTest {
    final TradeOrderGateway orders = mock(TradeOrderGateway.class);
    final TradeOrderItemGateway items = mock(TradeOrderItemGateway.class);
    final AfterSaleGateway afterSales = mock(AfterSaleGateway.class);
    final TradePaymentGateway payments = mock(TradePaymentGateway.class);
    final PaymentRefundGateway refunds = mock(PaymentRefundGateway.class);
    final InventoryConfirmationGateway inventory = mock(InventoryConfirmationGateway.class);
    final OrderEventGateway events = mock(OrderEventGateway.class);
    final AfterSalePromotionService attribution = mock(AfterSalePromotionService.class);
    final AfterSaleRefundCoordinator coordinator =
            new AfterSaleRefundCoordinator(
                    orders,
                    items,
                    afterSales,
                    payments,
                    refunds,
                    inventory,
                    mock(OrderLogService.class),
                    mock(AfterSaleLogService.class),
                    events,
                    mock(ActivityOrderLifecycleService.class),
                    mock(BrokerageService.class));
    final PaymentGateway cash = mock(PaymentGateway.class);
    final PaymentRefundCreateCmdExe executor =
            new PaymentRefundCreateCmdExe(coordinator, cash, refunds);

    TradeOrder order() {
        return new TradeOrder(
                7L,
                "TR0",
                1L,
                BigDecimal.ZERO,
                "PAID",
                List.of(),
                LocalDateTime.now(),
                100,
                0,
                null,
                false,
                null,
                Map.of(),
                BigDecimal.TEN,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "COMPLETE",
                1,
                List.of());
    }

    TradeOrderItem line(long id, long sku, int quantity) {
        return new TradeOrderItem(
                sku,
                "S",
                "商品",
                BigDecimal.TEN,
                quantity,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                3L,
                "SPU",
                null,
                null,
                id,
                false,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                100L,
                0L,
                0);
    }

    OrderRefundItem refundLine(long id, long sku, int q) {
        return new OrderRefundItem(
                id, sku, "商品", q, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    AfterSale af(String no, long item, long sku, int quantity, String state) {
        return new AfterSale(
                no,
                "TR0",
                "REQ" + no,
                1L,
                null,
                item,
                quantity,
                "REFUND_ONLY",
                BigDecimal.ZERO.setScale(2),
                "原因",
                state,
                null,
                null,
                null,
                LocalDateTime.now(),
                null,
                null,
                null,
                5L,
                7L,
                null,
                null,
                sku,
                3L,
                "SPU",
                null,
                null,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                50L,
                0L,
                1);
    }

    @BeforeEach
    void setup() {
        when(orders.lockHeader("TR0")).thenReturn(order());
        when(orders.findHeader("TR0")).thenReturn(order());
        when(orders.databaseNow()).thenReturn(LocalDateTime.now());
        when(items.items(7L)).thenReturn(List.of(line(11, 2, 2)));
        when(items.refundItems(7L)).thenReturn(List.of(refundLine(11, 2, 2)));
        when(items.recordRefunded(anyLong(), anyLong(), anyInt(), anyInt())).thenReturn(true);
        when(inventory.inventoryConfirmed("TR0")).thenReturn(true);
        when(afterSales.startPaymentRefund(any())).thenReturn(true);
        when(afterSales.completePaymentRefund(any(), any())).thenReturn(true);
        when(orders.transitionStatus("TR0", "PAID", "REFUNDED")).thenReturn(true);
    }

    void frozen(int qty, List<AfterSale> history) {
        var current = af("RF1", 11, 2, qty, "WAIT_REFUND");
        when(afterSales.afterSale("RF1")).thenReturn(current, af("RF1", 11, 2, qty, "REFUNDED"));
        when(afterSales.lockAfterSale("RF1")).thenReturn(current);
        when(afterSales.afterSales("TR0")).thenReturn(history);
    }

    @Test
    void zeroCashAfterSaleIsCreatedWithoutPaymentRecord() {
        var saved = new AtomicReference<AfterSale>();
        when(afterSales.afterSales("TR0")).thenReturn(List.of());
        doAnswer(
                        v -> {
                            saved.set(v.getArgument(0));
                            return null;
                        })
                .when(afterSales)
                .saveAfterSale(any());
        when(afterSales.afterSale(anyString())).thenAnswer(v -> saved.get());
        when(attribution.freeze(any(), eq(11L), eq(1), eq(0), any()))
                .thenReturn(
                        new AfterSalePromotionService.Plan(
                                BigDecimal.ZERO, BigDecimal.ZERO, 50, 0, List.of()));
        var service =
                new AfterSaleService(
                        orders,
                        items,
                        afterSales,
                        payments,
                        mock(AfterSaleLogService.class),
                        attribution);
        var v =
                service.create(
                        new AfterSaleCreateCmd("TR0", "REQ", 11L, 1, "REFUND_ONLY", "原因"),
                        TradeOperator.system());
        assertEquals(BigDecimal.ZERO.setScale(2), v.amount());
        assertNull(v.paymentNo());
        assertEquals("REQUESTED", v.status());
        assertEquals(50L, v.returnUsePoint());
        verifyNoInteractions(payments);
    }

    @Test
    void zeroCashRefundCompletesWithoutPaymentRefundAndMarksOrderRefunded() {
        frozen(2, List.of(af("RF1", 11, 2, 2, "REFUNDED")));
        var v = executor.execute("RF1");
        assertEquals("SUCCESS", v.status());
        assertNull(v.paymentNo());
        assertEquals(0, v.amount().signum());
        assertEquals("NONE", v.channel());
        assertNull(v.refundNo());
        verifyNoInteractions(cash, refunds, payments);
        verify(items).recordRefunded(7L, 11L, 0, 2);
        verify(orders).transitionStatus("TR0", "PAID", "REFUNDED");
        verify(events).afterSale(any(), any(), eq(true));
    }

    @Test
    void zeroRefundDispatchesBenefitsWithZeroAmount() {
        var af = af("RF1", 11, 2, 2, "REFUNDED");
        when(afterSales.afterSale("RF1")).thenReturn(af);
        when(events.replayAfterSale("RF1")).thenReturn(true);
        new RefundBenefitsService(afterSales, events).completeAfterSale("RF1");
        verify(events).replayAfterSale("RF1");
        verifyNoInteractions(cash, payments);
    }

    @Test
    void partialZeroRefundKeepsOrderPaid() {
        frozen(1, List.of(af("RF1", 11, 2, 1, "REFUNDED")));
        executor.execute("RF1");
        verify(items).recordRefunded(7L, 11L, 0, 1);
        verify(orders, never()).transitionStatus(any(), any(), any());
        verify(events).afterSale(any(), any(), eq(false));
    }

    @Test
    void multiLineZeroRefundKeepsOrderUntilAllLinesRefunded() {
        when(items.items(7L)).thenReturn(List.of(line(11, 2, 1), line(12, 4, 1)));
        when(items.refundItems(7L)).thenReturn(List.of(refundLine(11, 2, 1), refundLine(12, 4, 1)));
        frozen(1, List.of(af("RF1", 11, 2, 1, "REFUNDED")));
        executor.execute("RF1");
        verify(orders, never()).transitionStatus(any(), any(), any());
        verify(events).afterSale(any(), any(), eq(false));
    }

    @Test
    void multiLineZeroRefundMarksOrderWhenAllLinesRefunded() {
        when(items.items(7L)).thenReturn(List.of(line(11, 2, 1), line(12, 4, 1)));
        when(items.refundItems(7L)).thenReturn(List.of(refundLine(11, 2, 1), refundLine(12, 4, 1)));
        frozen(1, List.of(af("RF1", 11, 2, 1, "REFUNDED"), af("RF2", 12, 4, 1, "REFUNDED")));
        executor.execute("RF1");
        verify(orders).transitionStatus("TR0", "PAID", "REFUNDED");
        verify(events).afterSale(any(), any(), eq(true));
    }
}
