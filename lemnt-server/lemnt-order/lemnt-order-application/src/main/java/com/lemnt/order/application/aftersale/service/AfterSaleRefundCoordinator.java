package com.lemnt.order.application.aftersale.service;

import com.lemnt.order.application.brokerage.service.BrokerageService;
import com.lemnt.order.application.order.service.OrderLogService;
import com.lemnt.order.application.promotion.service.ActivityOrderLifecycleService;
import com.lemnt.order.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.order.domain.aftersale.model.AfterSale;
import com.lemnt.order.domain.event.gateway.OrderEventGateway;
import com.lemnt.order.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.order.domain.order.model.OrderRules;
import com.lemnt.order.domain.order.model.TradeOperator;
import com.lemnt.order.domain.order.model.TradeOrder;
import com.lemnt.order.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.order.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.order.domain.paymentrefund.model.PaymentRefund;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

/**
 * 退单金额在创建时已冻结；这里只协调原计划，成功时一次累计数量并登记订单事实。
 */
@Component
@RequiredArgsConstructor
public class AfterSaleRefundCoordinator {
    private final TradeOrderGateway orders;
    private final TradeOrderItemGateway items;
    private final AfterSaleGateway afterSales;
    private final TradePaymentGateway payments;
    private final PaymentRefundGateway refunds;
    private final InventoryConfirmationGateway inventory;
    private final OrderLogService logs;
    private final AfterSaleLogService afterSaleLogs;
    private final OrderEventGateway events;
    private final ActivityOrderLifecycleService activities;
    private final BrokerageService brokerage;

    /**
     * 同一订单锁下登记执行状态，提交之后才能调用资金服务。
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public AfterSale prepare(String no, TradeOperator operator) {
        var initial = required(no);
        var order = orders.lockHeader(initial.orderNo());
        var af = afterSales.lockAfterSale(initial.refundNo());
        frozen(af, order);
        if ("REFUNDED".equals(af.status())) return af;
        if (!Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status())
            || !Set.of("WAIT_REFUND", "REFUNDING").contains(af.status()))
            throw new IllegalStateException("售后须完成审核和退货接收后才能退款");
        if (!inventory.inventoryConfirmed(order.orderNo()))
            throw new IllegalStateException("支付后权益尚未全部确认，请稍后重试");
        if (af.amount().signum() > 0) verifyPayment(af, order);
        if ("WAIT_REFUND".equals(af.status())) {
            if (!afterSales.startPaymentRefund(no)) throw new IllegalStateException("售后状态已变化");
            afterSaleLogs.record(af, "WAIT_REFUND", "REFUNDING", 30, operator, "发起原路退款");
        }
        if (af.amount().signum() == 0) finish(af, order, operator);
        return afterSales.afterSale(no);
    }

    /**
     * 原冻结方案只读查询；历史缺失不得按当前数据补齐。
     */
    public AfterSale plan(String no) {
        var af = required(no);
        frozen(af, orders.findHeader(af.orderNo()));
        return af;
    }

    private AfterSale required(String no) {
        var af = afterSales.afterSale(OrderRules.text(no, 40));
        if (af == null) throw new IllegalArgumentException("售后单不存在");
        return af;
    }

    private void frozen(AfterSale af, TradeOrder order) {
        if (order == null
            || !Objects.equals(order.id(), af.orderId())
            || !Objects.equals(order.memberId(), af.memberId())
            || !Integer.valueOf(1).equals(af.refundRuleVersion())
            || af.orderItemId() == null
            || af.skuId() == null
            || af.quantity() == null
            || af.quantity() < 1
            || af.amount() == null
            || af.amount().signum() < 0
            || af.amount().scale() > 2
            || af.returnUsePoint() == null
            || af.returnUsePoint() < 0
            || af.deductGivePoint() == null
            || af.deductGivePoint() < 0)
            throw new IllegalStateException("原售后冻结方案不完整或不一致，请先审核迁移");
        var line =
            items.items(order.id()).stream()
                .filter(v -> af.orderItemId().equals(v.orderItemId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("售后商品行缺失"));
        if (!af.skuId().equals(line.productId())
            || af.quantity() > line.quantity()
            || line.refundedQuantity() == null)
            throw new IllegalStateException("售后商品或历史已退数量未核对");
    }

    private void verifyPayment(AfterSale af, TradeOrder order) {
        var paid =
            payments.findByOrderNo(order.orderNo()).stream()
                .filter(v -> "SUCCESS".equals(v.status()))
                .toList();
        if (paid.size() != 1
            || !Objects.equals(paid.get(0).paymentNo(), af.paymentNo())
            || paid.get(0).amount().compareTo(order.totalAmount()) != 0
            || af.amount().compareTo(order.totalAmount()) > 0
            || paid.get(0).channelTradeNo() == null
            || paid.get(0).channelTradeNo().isBlank())
            throw new IllegalStateException("原成功支付与冻结售后不一致，请先对账");
    }

    /**
     * 只接受资金服务可查询的真实成功记录，不信任消息中单独传入的状态或金额。
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void cashSucceeded(PaymentRefund fact) {
        var initial = required(fact.afterSaleNo());
        var order = orders.lockHeader(initial.orderNo());
        var af = afterSales.lockAfterSale(initial.refundNo());
        frozen(af, order);
        var saved = refunds.findByAfterSaleNo(af.refundNo());
        if (saved == null
            || !"SUCCESS".equals(saved.status())
            || !saved.equals(fact)
            || af.amount().signum() <= 0
            || !Objects.equals(af.paymentNo(), fact.paymentNo())
            || !af.orderNo().equals(fact.orderNo())
            || af.amount().compareTo(fact.amount()) != 0)
            throw new IllegalStateException("现金退款事实与原方案不一致");
        verifyPayment(af, order);
        if ("REFUNDED".equals(af.status())) return;
        if (!"REFUNDING".equals(af.status())
            || !Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status()))
            throw new IllegalStateException("售后尚未登记退款执行");
        finish(af, order, TradeOperator.system());
    }

    /**
     * 失败同样核对资金原记录；只追加审计，不完成售后或释放履约限制。
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void cashFailed(PaymentRefund fact, TradeOperator operator) {
        var initial = required(fact.afterSaleNo());
        var order = orders.lockHeader(initial.orderNo());
        var af = afterSales.lockAfterSale(initial.refundNo());
        frozen(af, order);
        var saved = refunds.findByAfterSaleNo(af.refundNo());
        if (saved == null
            || !"FAILED".equals(saved.status())
            || !saved.equals(fact)
            || af.amount().signum() <= 0
            || !Objects.equals(af.paymentNo(), fact.paymentNo())
            || !af.orderNo().equals(fact.orderNo())
            || af.amount().compareTo(fact.amount()) != 0
            || !"REFUNDING".equals(af.status()))
            throw new IllegalStateException("失败退款事实与原方案不一致");
        verifyPayment(af, order);
        afterSaleLogs.refundFailed(af, operator);
    }

    private void finish(AfterSale af, TradeOrder order, TradeOperator operator) {
        if (!afterSales.completePaymentRefund(af.refundNo(), orders.databaseNow()))
            throw new IllegalStateException("售后退款成功状态冲突");
        var history =
            afterSales.afterSales(order.orderNo()).stream()
                .filter(v -> "REFUNDED".equals(v.status()))
                .toList();
        if (history.stream().anyMatch(v -> v.orderItemId() == null || v.quantity() == null))
            throw new IllegalStateException("历史退款数量未知，请先对账");
        int previous =
            history.stream()
                .filter(
                    v ->
                        af.orderItemId().equals(v.orderItemId())
                            && !af.refundNo().equals(v.refundNo()))
                .mapToInt(AfterSale::quantity)
                .sum();
        if (!items.recordRefunded(order.id(), af.orderItemId(), previous, af.quantity()))
            throw new IllegalStateException("订单明细已退数量更新冲突");
        var cash = history.stream().map(AfterSale::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (cash.compareTo(order.totalAmount()) > 0) throw new IllegalStateException("累计退款超过订单实付");
        var lines = items.refundItems(order.id());
        boolean full =
            !lines.isEmpty()
                && lines.stream()
                .allMatch(
                    l ->
                        history.stream()
                            .filter(
                                v ->
                                    l.id().equals(
                                        v
                                            .orderItemId()))
                            .mapToInt(AfterSale::quantity)
                            .sum()
                            == l.quantity());
        if (full) {
            if (cash.compareTo(order.totalAmount()) != 0
                || !orders.transitionStatus(order.orderNo(), order.status(), "REFUNDED"))
                throw new IllegalStateException("全退现金合计或订单状态不一致");
            logs.record(order, order.status(), "REFUNDED", 43, operator, "订单商品全部售后");
        }
        activities.refunded(order.orderNo(), af.refundNo(), af.skuId(), af.quantity(), full);
        events.afterSale(af, order, full);
        if (full) brokerage.cancelOrderCommission(order.orderNo());
        else brokerage.recallOrderCommission(order.orderNo(), cash, order.totalAmount());
        afterSaleLogs.record(
            afterSales.afterSale(af.refundNo()), "REFUNDING", "REFUNDED", 31, operator, "退款成功");
    }
}
