package com.lemnt.pay.application.paymentrefund.service;

import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.pay.client.event.PayRefundSuccessEvent;
import com.lemnt.pay.domain.event.gateway.MessageOutboxGateway;
import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.order.model.FrozenRefundPlan;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PaymentOperator;
import com.lemnt.pay.domain.payment.model.PaymentRules;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** 支付库只保存原路退款资金事实；售后冻结、数量与状态由order拥有。 */
@Component
@RequiredArgsConstructor
public class PaymentRefundService {
    private final OrderPaymentGateway orders;
    private final TradePaymentGateway payments;
    private final PaymentRefundGateway refunds;
    private final MessageOutboxGateway outbox;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public PaymentRefund prepare(String afterSaleNo) {
        return prepare(afterSaleNo, PaymentOperator.system());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public PaymentRefund prepare(String afterSaleNo, PaymentOperator operator) {
        var plan = orders.prepareRefund(PaymentRules.text(afterSaleNo, 40), operator);
        requirePlan(plan);
        if (plan.amount().signum() == 0) {
            if (!"REFUNDED".equals(plan.status()))
                throw new IllegalStateException("零现金售后尚未由订单服务完成");
            return new PaymentRefund(
                    null,
                    plan.afterSaleNo(),
                    plan.paymentNo(),
                    plan.orderNo(),
                    "NONE",
                    plan.amount(),
                    "SUCCESS",
                    null);
        }
        if (!Set.of("REFUNDING", "REFUNDED").contains(plan.status()))
            throw new IllegalStateException("售后未准备原路退款请求");
        var payment = payments.lockByPaymentNo(plan.paymentNo());
        var order = orders.findByOrderNo(plan.orderNo());
        if (payment == null
                || order == null
                || !"SUCCESS".equals(payment.status())
                || !plan.orderNo().equals(payment.orderNo())
                || !plan.memberId().equals(order.memberId())
                || payment.amount().compareTo(order.totalAmount()) != 0
                || blank(payment.channelTradeNo()))
            throw new IllegalStateException("原支付、会员或订单金额不一致");
        var existing = refunds.findByAfterSaleNo(plan.afterSaleNo());
        if (existing != null) {
            if (!plan.paymentNo().equals(existing.paymentNo())
                    || !plan.orderNo().equals(existing.orderNo())
                    || plan.amount().compareTo(existing.amount()) != 0)
                throw new IllegalStateException("重复售后退款内容不一致");
            if ("FAILED".equals(existing.status())) {
                if (refunds.totalAmount(payment.paymentNo(), existing.refundNo(), false)
                                .add(existing.amount())
                                .compareTo(payment.amount())
                        > 0) throw new IllegalStateException("重试退款超过原支付可退额度");
                var retry =
                        new PaymentRefund(
                                existing.refundNo(),
                                existing.afterSaleNo(),
                                existing.paymentNo(),
                                existing.orderNo(),
                                existing.channel(),
                                existing.amount(),
                                "WAITING",
                                existing.channelRefundNo());
                if (!refunds.updateResult(retry)) throw new IllegalStateException("退款重试额度预留失败");
                return retry;
            }
            return existing;
        }
        if (!"REFUNDING".equals(plan.status())
                || refunds.totalAmount(payment.paymentNo(), null, false)
                                .add(plan.amount())
                                .compareTo(payment.amount())
                        > 0) throw new IllegalStateException("累计退款额度或原售后状态不合法");
        var refund =
                new PaymentRefund(
                        "PR"
                                + UUID.randomUUID()
                                        .toString()
                                        .replace("-", "")
                                        .toUpperCase(Locale.ROOT),
                        plan.afterSaleNo(),
                        payment.paymentNo(),
                        payment.orderNo(),
                        payment.channel(),
                        plan.amount(),
                        "WAITING",
                        null);
        refunds.save(refund);
        return refund;
    }

    /** 仅为兼容存量调用方，pay从不产生AFTER_SALE权益任务。 */
    public String taskType(PaymentRefund refund) {
        return "REFUND";
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public PaymentRefund complete(PaymentRefund original, PaymentRefundResult result) {
        if (original == null
                || result == null
                || !original.refundNo().equals(result.refundNo())
                || !original.paymentNo().equals(result.paymentNo())
                || !original.channel().equals(result.channel())
                || result.amount() == null
                || original.amount().compareTo(result.amount()) != 0
                || result.status() == null
                || !Set.of("WAITING", "SUCCESS", "FAILED").contains(result.status())
                || ("SUCCESS".equals(result.status()) && blank(result.channelRefundNo()))
                || (result.channelRefundNo() != null
                        && (result.channelRefundNo().isBlank()
                                || result.channelRefundNo().length() > 128))) {
            throw new IllegalStateException("渠道退款结果与本地退款单不一致");
        }
        var plan = orders.refundPlan(original.afterSaleNo());
        requirePlan(plan);
        if (!plan.orderNo().equals(original.orderNo())
                || !plan.paymentNo().equals(original.paymentNo())
                || plan.amount().compareTo(original.amount()) != 0)
            throw new IllegalStateException("原售后冻结退款请求不一致");
        var payment = payments.lockByPaymentNo(original.paymentNo());
        var current = refunds.find(original.refundNo());
        if (payment == null
                || current == null
                || !"SUCCESS".equals(payment.status())
                || !current.afterSaleNo().equals(plan.afterSaleNo()))
            throw new IllegalStateException("原资金记录缺失");
        if ("SUCCESS".equals(current.status())) {
            if ("SUCCESS".equals(result.status())
                    && !current.channelRefundNo().equals(result.channelRefundNo()))
                throw new IllegalStateException("重复退款通知的渠道单号不一致");
            return current;
        }
        if ("FAILED".equals(current.status()) && "FAILED".equals(result.status())) return current;
        if (current.channelRefundNo() != null
                && result.channelRefundNo() != null
                && !current.channelRefundNo().equals(result.channelRefundNo()))
            throw new IllegalStateException("渠道退款编号与已受理记录不一致");
        var updated =
                new PaymentRefund(
                        current.refundNo(),
                        current.afterSaleNo(),
                        current.paymentNo(),
                        current.orderNo(),
                        current.channel(),
                        current.amount(),
                        result.status(),
                        result.channelRefundNo() == null
                                ? current.channelRefundNo()
                                : result.channelRefundNo());
        if (!refunds.updateResult(updated)) throw new IllegalStateException("退款状态写入冲突");
        if ("SUCCESS".equals(updated.status())) {
            outbox.enqueue(
                    CommerceEventTopics.PAY_REFUND_SUCCESS,
                    updated.refundNo(),
                    updated.orderNo(),
                    JsonUtils.toJsonStr(
                            new PayRefundSuccessEvent(
                                    updated.refundNo(),
                                    updated.afterSaleNo(),
                                    updated.paymentNo(),
                                    updated.orderNo(),
                                    plan.memberId(),
                                    updated.channel(),
                                    updated.amount(),
                                    updated.channelRefundNo())));
        }
        return updated;
    }

    private void requirePlan(FrozenRefundPlan plan) {
        if (plan == null
                || plan.afterSaleNo() == null
                || plan.orderNo() == null
                || plan.memberId() == null
                || plan.amount() == null
                || plan.amount().signum() < 0) throw new IllegalStateException("原售后冻结请求缺失或不合法");
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
