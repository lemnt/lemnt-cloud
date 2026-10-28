package com.lemnt.trade.application.paymentrefund.service;

import com.lemnt.trade.application.aftersale.service.AfterSaleLogService;
import com.lemnt.trade.application.order.service.OrderLogService;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.OrderRules;
import com.lemnt.trade.domain.order.model.TradeOperator;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefundResult;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** 售后单与支付退款交易的本地事务编排，渠道调用在事务外进行。 */
@Component
@RequiredArgsConstructor
public class PaymentRefundService {
    private final TradeOrderGateway orders;
    private final AfterSaleGateway afterSales;
    private final TradePaymentGateway payments;
    private final PaymentRefundGateway refunds;
    private final InventoryConfirmationGateway inventoryConfirmation;
    private final OrderLogService logs;
    private final AfterSaleLogService afterSaleLogs;

    /** 锁定订单后校验售后与原支付，独立提交唯一退款单再调用渠道。 */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public PaymentRefund prepare(String afterSaleNo) {
        return prepare(afterSaleNo, TradeOperator.system());
    }

    /** 发起退款保留实际后台操作者。 */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public PaymentRefund prepare(String afterSaleNo, TradeOperator operator) {
        var initial = afterSales.afterSale(OrderRules.text(afterSaleNo, 40));
        if (initial == null) {
            throw new IllegalArgumentException("售后单不存在");
        }
        var order = orders.lockHeader(initial.orderNo());
        afterSales.lockAfterSale(initial.refundNo());
        var afterSale = afterSales.afterSale(initial.refundNo());
        var existing = refunds.findByAfterSaleNo(afterSale.refundNo());
        if (existing != null) {
            return existing;
        }
        boolean ready = "WAIT_REFUND".equals(afterSale.status());
        if (order == null || !Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status()) || !ready) {
            throw new IllegalStateException("售后须同意退款，退货退款须先确认收到退货");
        }
        if (!inventoryConfirmation.inventoryConfirmed(order.orderNo())) {
            throw new IllegalStateException("支付后处理尚未完成，请先重试支付通知");
        }
        var successful = payments.findByOrderNo(order.orderNo()).stream()
                .filter(payment -> "SUCCESS".equals(payment.status())).toList();
        if (successful.size() != 1 || !successful.get(0).paymentNo().equals(afterSale.paymentNo())
                || successful.get(0).amount().compareTo(order.totalAmount()) != 0
                || afterSale.amount().signum() <= 0
                || afterSale.amount().compareTo(order.totalAmount()) > 0
                || !order.memberId().equals(afterSale.memberId())) {
            throw new IllegalStateException("售后金额、会员或成功支付记录不一致，请先对账");
        }
        var payment = successful.get(0);
        if (refunds.totalAmount(payment.paymentNo(), null, false).add(afterSale.amount())
                .compareTo(payment.amount()) > 0) {
            throw new IllegalStateException("累计支付退款金额超过原支付金额");
        }
        if (payment.channelTradeNo() == null || payment.channelTradeNo().isBlank()) {
            throw new IllegalStateException("原支付渠道交易编号缺失");
        }
        var refund = new PaymentRefund("PR" + UUID.randomUUID().toString().replace("-", "")
                .toUpperCase(Locale.ROOT), afterSale.refundNo(), payment.paymentNo(), order.orderNo(),
                payment.channel(), afterSale.amount(), "WAITING", null);
        refunds.save(refund);
        if (!afterSales.startPaymentRefund(afterSale.refundNo())) {
            throw new IllegalStateException("售后单状态已变化");
        }
        afterSaleLogs.record(afterSale, afterSale.status(), "REFUNDING", 30, operator, "发起原路退款");
        return refund;
    }

    /** 核对渠道响应绑定，仅成功结果可以完成售后和订单。 */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public PaymentRefund complete(PaymentRefund original, PaymentRefundResult result) {
        if (result == null || !original.refundNo().equals(result.refundNo())
                || !original.paymentNo().equals(result.paymentNo()) || !original.channel().equals(result.channel())
                || result.amount() == null || original.amount().compareTo(result.amount()) != 0
                || result.status() == null || !Set.of("WAITING", "SUCCESS", "FAILED").contains(result.status())
                || ("SUCCESS".equals(result.status())
                    && (result.channelRefundNo() == null || result.channelRefundNo().isBlank()))) {
            throw new IllegalStateException("渠道退款结果与本地退款单不一致");
        }
        if (result.channelRefundNo() != null && (result.channelRefundNo().isBlank()
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
        if (current.channelRefundNo() != null && result.channelRefundNo() != null
                && !current.channelRefundNo().equals(result.channelRefundNo())) {
            throw new IllegalStateException("渠道退款编号与已受理记录不一致");
        }
        if (!Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status())
                || !"REFUNDING".equals(afterSale.status())) {
            throw new IllegalStateException("退款关联订单或售后单状态不一致");
        }
        // 已受理的渠道编号不被延迟到达的空结果清掉。
        String channelNo = result.channelRefundNo() == null ? current.channelRefundNo() : result.channelRefundNo();
        var updated = new PaymentRefund(current.refundNo(), current.afterSaleNo(), current.paymentNo(),
                current.orderNo(), current.channel(), current.amount(), result.status(), channelNo);
        if (!refunds.updateResult(updated)) {
            throw new IllegalStateException("退款交易状态已变化");
        }
        if ("SUCCESS".equals(result.status())) {
            if (!afterSales.completePaymentRefund(afterSale.refundNo(), orders.databaseNow())) {
                throw new IllegalStateException("退款成功状态写入冲突");
            }
            var fullAfterSale = afterSales.afterSale(afterSale.refundNo());
            afterSaleLogs.record(fullAfterSale, "REFUNDING", "REFUNDED", 31, TradeOperator.system(), "退款成功");
            var refunded = refunds.totalAmount(current.paymentNo(), current.refundNo(), true).add(current.amount());
            if (refunded.compareTo(order.totalAmount()) > 0) {
                throw new IllegalStateException("累计退款超过订单实付金额");
            }
            // 部分退款保留原履约阶段，累计全额退款才将整单标记为已退款。
            if (refunded.compareTo(order.totalAmount()) == 0
                    && !orders.transitionStatus(order.orderNo(), order.status(), "REFUNDED")) {
                throw new IllegalStateException("订单退款状态写入冲突");
            }
            if (refunded.compareTo(order.totalAmount()) == 0) {
                logs.record(order, order.status(), "REFUNDED", 43, TradeOperator.system(), "订单全部售后，自动取消订单");
            }
        }
        if ("FAILED".equals(result.status())) {
            afterSaleLogs.record(afterSales.afterSale(afterSale.refundNo()), "REFUNDING", "REFUNDING", 32,
                    TradeOperator.system(), "退款失败");
        }
        return updated;
    }
}
