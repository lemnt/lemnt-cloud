package com.lemnt.trade.application.paymentrefund.service;

import com.lemnt.trade.application.aftersale.service.AfterSaleLogService;
import com.lemnt.trade.application.order.service.OrderLogService;
import com.lemnt.trade.application.payment.service.PayNotifyTaskService;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.aftersale.model.AfterSale;
import com.lemnt.trade.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.trade.domain.order.model.OrderRules;
import com.lemnt.trade.domain.order.model.TradeOperator;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefundResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** 售后单与支付退款交易的本地事务编排，渠道调用在事务外进行。 */
@Component
public class PaymentRefundService {
    private final TradeOrderGateway orders;
    private final AfterSaleGateway afterSales;
    private final TradePaymentGateway payments;
    private final TradeOrderItemGateway items;
    private final PaymentRefundGateway refunds;
    private final InventoryConfirmationGateway inventoryConfirmation;
    private final OrderLogService logs;
    private final AfterSaleLogService afterSaleLogs;

    private final PayNotifyTaskService tasks;

    @Autowired
    public PaymentRefundService(
            TradeOrderGateway orders,
            AfterSaleGateway afterSales,
            TradePaymentGateway payments,
            TradeOrderItemGateway items,
            PaymentRefundGateway refunds,
            InventoryConfirmationGateway inventoryConfirmation,
            OrderLogService logs,
            AfterSaleLogService afterSaleLogs,
            PayNotifyTaskService tasks) {
        this.orders = orders;
        this.afterSales = afterSales;
        this.payments = payments;
        this.items = items;
        this.refunds = refunds;
        this.inventoryConfirmation = inventoryConfirmation;
        this.logs = logs;
        this.afterSaleLogs = afterSaleLogs;
        this.tasks = tasks;
    }

    /** 兼容历史单元组装；生产使用带可靠任务的完整构造。 */
    public PaymentRefundService(
            TradeOrderGateway orders,
            AfterSaleGateway afterSales,
            TradePaymentGateway payments,
            TradeOrderItemGateway items,
            PaymentRefundGateway refunds,
            InventoryConfirmationGateway inventoryConfirmation,
            OrderLogService logs,
            AfterSaleLogService afterSaleLogs) {
        this(
                orders,
                afterSales,
                payments,
                items,
                refunds,
                inventoryConfirmation,
                logs,
                afterSaleLogs,
                null);
    }

    /** 锁定订单后校验售后与原支付，独立提交唯一退款单再调用渠道。 */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class)
    public PaymentRefund prepare(String afterSaleNo) {
        return prepare(afterSaleNo, TradeOperator.system());
    }

    /** 发起退款保留实际后台操作者。 */
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            isolation = Isolation.READ_COMMITTED,
            rollbackFor = Exception.class)
    public PaymentRefund prepare(String afterSaleNo, TradeOperator operator) {
        var initial = afterSales.afterSale(OrderRules.text(afterSaleNo, 40));
        if (initial == null) {
            throw new IllegalArgumentException("售后单不存在");
        }
        var order = orders.lockHeader(initial.orderNo());
        var afterSale = afterSales.lockAfterSale(initial.refundNo());
        var existing = refunds.findByAfterSaleNo(afterSale.refundNo());
        if (existing != null) {
            return existing;
        }
        if ("REFUNDED".equals(afterSale.status()) && afterSale.amount().signum() == 0) {
            return withoutCash(afterSale);
        }
        boolean ready = "WAIT_REFUND".equals(afterSale.status());
        if (order == null
                || !Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status())
                || !ready) {
            throw new IllegalStateException("售后须同意退款，退货退款须先确认收到退货");
        }
        if (!inventoryConfirmation.inventoryConfirmed(order.orderNo())) {
            throw new IllegalStateException("支付后处理尚未完成，请先重试支付通知");
        }
        if (afterSale.amount().signum() == 0) {
            return zeroRefund(afterSale, order, operator);
        }
        var successful =
                payments.findByOrderNo(order.orderNo()).stream()
                        .filter(payment -> "SUCCESS".equals(payment.status()))
                        .toList();
        if (successful.size() != 1
                || !successful.get(0).paymentNo().equals(afterSale.paymentNo())
                || successful.get(0).amount().compareTo(order.totalAmount()) != 0
                || afterSale.amount().signum() <= 0
                || afterSale.amount().compareTo(order.totalAmount()) > 0
                || !order.memberId().equals(afterSale.memberId())) {
            throw new IllegalStateException("售后金额、会员或成功支付记录不一致，请先对账");
        }
        var payment = successful.get(0);
        if (refunds.totalAmount(payment.paymentNo(), null, false)
                        .add(afterSale.amount())
                        .compareTo(payment.amount())
                > 0) {
            throw new IllegalStateException("累计支付退款金额超过原支付金额");
        }
        if (payment.channelTradeNo() == null || payment.channelTradeNo().isBlank()) {
            throw new IllegalStateException("原支付渠道交易编号缺失");
        }
        var refund =
                new PaymentRefund(
                        "PR"
                                + UUID.randomUUID()
                                        .toString()
                                        .replace("-", "")
                                        .toUpperCase(Locale.ROOT),
                        afterSale.refundNo(),
                        payment.paymentNo(),
                        order.orderNo(),
                        payment.channel(),
                        afterSale.amount(),
                        "WAITING",
                        null);
        refunds.save(refund);
        if (!afterSales.startPaymentRefund(afterSale.refundNo())) {
            throw new IllegalStateException("售后单状态已变化");
        }
        afterSaleLogs.record(afterSale, afterSale.status(), "REFUNDING", 30, operator, "发起原路退款");
        return refund;
    }

    /** 零现金份额无需渠道退款流水，售后成功与权益任务同事务提交。 */
    private PaymentRefund zeroRefund(
            AfterSale afterSale, TradeOrder order, TradeOperator operator) {
        if (afterSale.amount().signum() != 0 || !order.memberId().equals(afterSale.memberId())) {
            throw new IllegalStateException("零现金售后金额或会员不一致");
        }
        if (!afterSales.startPaymentRefund(afterSale.refundNo())) {
            throw new IllegalStateException("售后状态已变化");
        }
        finishAfterSale(afterSale, order, operator);
        return withoutCash(afterSale);
    }

    /** 返回无现金执行结果，refundNo为空表示没有创建支付退款交易。 */
    private PaymentRefund withoutCash(AfterSale afterSale) {
        return new PaymentRefund(
                null,
                afterSale.refundNo(),
                afterSale.paymentNo(),
                afterSale.orderNo(),
                "NONE",
                afterSale.amount(),
                "SUCCESS",
                null);
    }

    /** 现金与零现金统一完成数量事实，不能仅凭退完现金提前结束整单。 */
    private void finishAfterSale(AfterSale afterSale, TradeOrder order, TradeOperator operator) {
        if (!afterSales.completePaymentRefund(afterSale.refundNo(), orders.databaseNow())) {
            throw new IllegalStateException("退款成功状态写入冲突");
        }
        var history =
                afterSales.afterSales(order.orderNo()).stream()
                        .filter(h -> "REFUNDED".equals(h.status()))
                        .toList();
        var lines = items.refundItems(order.id());
        if (afterSale.orderItemId() != null) {
            int previous =
                    history.stream()
                            .filter(
                                    h ->
                                            afterSale.orderItemId().equals(h.orderItemId())
                                                    && !afterSale.refundNo().equals(h.refundNo()))
                            .mapToInt(AfterSale::quantity)
                            .sum();
            if (lines.stream().noneMatch(l -> afterSale.orderItemId().equals(l.id()))
                    || !items.recordRefunded(
                            order.id(), afterSale.orderItemId(), previous, afterSale.quantity())) {
                throw new IllegalStateException("订单明细已退数量更新冲突");
            }
        }
        var cash = history.stream().map(AfterSale::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (cash.compareTo(order.totalAmount()) > 0) {
            throw new IllegalStateException("累计退款超过订单实付");
        }
        boolean wholeLegacy = history.stream().anyMatch(h -> h.orderItemId() == null);
        boolean full =
                wholeLegacy
                        ? cash.compareTo(order.totalAmount()) == 0
                        : !lines.isEmpty()
                                && lines.stream()
                                        .allMatch(
                                                line ->
                                                        history.stream()
                                                                        .filter(
                                                                                h ->
                                                                                        line.id()
                                                                                                .equals(
                                                                                                        h
                                                                                                                .orderItemId()))
                                                                        .mapToInt(
                                                                                AfterSale::quantity)
                                                                        .sum()
                                                                == line.quantity());
        if (full) {
            if (cash.compareTo(order.totalAmount()) != 0) {
                throw new IllegalStateException("商品全退但现金合计不一致");
            }
            if (!orders.transitionStatus(order.orderNo(), order.status(), "REFUNDED")) {
                throw new IllegalStateException("订单退款状态写入冲突");
            }
            logs.record(order, order.status(), "REFUNDED", 43, operator, "订单商品全部售后");
        }
        afterSaleLogs.record(
                afterSales.afterSale(afterSale.refundNo()),
                "REFUNDING",
                "REFUNDED",
                31,
                operator,
                "退款成功");
        if (tasks != null) {
            // 同trade事务写入；远程调用由提交后的执行器或到期任务处理。
            if (afterSale.refundRuleVersion() != null || afterSale.amount().signum() == 0) {
                tasks.enqueue("AFTER_SALE", afterSale.refundNo(), order.orderNo());
            } else {
                var paymentRefund = refunds.findByAfterSaleNo(afterSale.refundNo());
                tasks.enqueue("REFUND", paymentRefund.refundNo(), order.orderNo());
            }
        }
    }

    /** 兼容存量PR任务，新退单按RF入队，不创建两份权益任务。 */
    public String taskType(PaymentRefund refund) {
        var afterSale = afterSales.afterSale(refund.afterSaleNo());
        return afterSale != null
                        && (afterSale.refundRuleVersion() != null || refund.refundNo() == null)
                ? "AFTER_SALE"
                : "REFUND";
    }

    /** 核对渠道响应绑定，仅成功结果可以完成售后和订单。 */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public PaymentRefund complete(PaymentRefund original, PaymentRefundResult result) {
        if (result == null
                || !original.refundNo().equals(result.refundNo())
                || !original.paymentNo().equals(result.paymentNo())
                || !original.channel().equals(result.channel())
                || result.amount() == null
                || original.amount().compareTo(result.amount()) != 0
                || result.status() == null
                || !Set.of("WAITING", "SUCCESS", "FAILED").contains(result.status())
                || ("SUCCESS".equals(result.status())
                        && (result.channelRefundNo() == null
                                || result.channelRefundNo().isBlank()))) {
            throw new IllegalStateException("渠道退款结果与本地退款单不一致");
        }
        if (result.channelRefundNo() != null
                && (result.channelRefundNo().isBlank()
                        || result.channelRefundNo().length() > 128)) {
            throw new IllegalStateException("渠道退款编号不合法");
        }
        var order = orders.lockHeader(original.orderNo());
        var afterSale = afterSales.lockAfterSale(original.afterSaleNo());
        var current = refunds.find(original.refundNo());
        if (current == null || order == null || afterSale == null) {
            throw new IllegalStateException("退款关联记录不存在");
        }
        if ("SUCCESS".equals(current.status())) {
            if ("SUCCESS".equals(result.status())
                    && !current.channelRefundNo().equals(result.channelRefundNo())) {
                throw new IllegalStateException("重复退款通知的渠道单号不一致");
            }
            return current;
        }
        if ("FAILED".equals(current.status()) && "FAILED".equals(result.status())) {
            return current;
        }
        if (current.channelRefundNo() != null
                && result.channelRefundNo() != null
                && !current.channelRefundNo().equals(result.channelRefundNo())) {
            throw new IllegalStateException("渠道退款编号与已受理记录不一致");
        }
        if (!Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status())
                || !"REFUNDING".equals(afterSale.status())) {
            throw new IllegalStateException("退款关联订单或售后单状态不一致");
        }
        // 已受理的渠道编号不被延迟到达的空结果清掉。
        String channelNo =
                result.channelRefundNo() == null
                        ? current.channelRefundNo()
                        : result.channelRefundNo();
        var updated =
                new PaymentRefund(
                        current.refundNo(),
                        current.afterSaleNo(),
                        current.paymentNo(),
                        current.orderNo(),
                        current.channel(),
                        current.amount(),
                        result.status(),
                        channelNo);
        if (!refunds.updateResult(updated)) {
            throw new IllegalStateException("退款交易状态已变化");
        }
        if ("SUCCESS".equals(result.status())) {
            finishAfterSale(afterSale, order, TradeOperator.system());
        }
        if ("FAILED".equals(result.status())) {
            afterSaleLogs.record(
                    afterSales.afterSale(afterSale.refundNo()),
                    "REFUNDING",
                    "REFUNDING",
                    32,
                    TradeOperator.system(),
                    "退款失败");
        }
        return updated;
    }
}
