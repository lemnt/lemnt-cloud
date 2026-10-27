package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.outbox.gateway.TradeOutboxGateway;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.payment.model.PaymentRecord;
import com.lemnt.trade.domain.payment.model.VerifiedPaymentNotification;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * Application boundary for a notification already authenticated by a concrete channel adapter. Do
 * not bind this executor directly to an unauthenticated HTTP callback.
 */
@Component
@RequiredArgsConstructor
public class PaymentNotifyCmdExe {
    private final OrderPersistenceService orderPersistenceService;
    private final TradeOrderGateway tradeOrderGateway;
    private final TradePaymentGateway tradePaymentGateway;
    private final TradeOutboxGateway tradeOutboxGateway;

    /**
     * 执行已核验支付通知。
     */
    @Transactional(rollbackFor = Exception.class)
    public void execute(VerifiedPaymentNotification notification) {
        if (notification == null
                || blank(notification.paymentNo())
                || blank(notification.orderNo())
                || blank(notification.channel())
                || blank(notification.channelTradeNo())
                || blank(notification.notifyId())
                || notification.amount() == null
                || notification.amount().signum() <= 0) {
            throw new IllegalArgumentException("支付通知参数不合法");
        }

        TradeOrder order = orderPersistenceService.findByOrderNoForUpdate(notification.orderNo());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        PaymentRecord payment = tradePaymentGateway.findByPaymentNo(notification.paymentNo());
        if (payment == null) {
            throw new IllegalArgumentException("支付单不存在");
        }
        if (!payment.orderNo().equals(notification.orderNo())
                || !payment.channel().equals(notification.channel())
                || payment.amount().compareTo(notification.amount()) != 0) {
            throw new IllegalArgumentException("支付通知与本地支付单不匹配");
        }

        if (order.totalAmount().compareTo(payment.amount()) != 0) {
            throw new IllegalStateException("支付单金额与订单应付金额不一致");
        }

        if ("SUCCESS".equals(payment.status())) {
            if (!Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status())) {
                throw new IllegalStateException("成功支付与订单状态不一致，请先对账");
            }

            // Same payment callback is idempotent; mismatched replay is rejected.
            if (payment.channelTradeNo() != null
                    && payment.channelTradeNo().equals(notification.channelTradeNo())) {
                return ;
            }
            throw new IllegalStateException("支付单已成功，但收到不同的重复通知");
        }
        if (!"WAITING".equals(payment.status())) {
            throw new IllegalStateException("支付单状态不允许完成");
        }

        if (!"PENDING_PAYMENT".equals(order.status())) {
            throw new IllegalStateException("订单已关闭或已支付，不能再确认支付");
        }
        if (!tradePaymentGateway.markSuccess(notification)) {
            throw new IllegalStateException("支付单状态已变化，请重试");
        }
        if (!tradeOrderGateway.transitionStatus(order.orderNo(), "PENDING_PAYMENT", "PAID")) {
            throw new IllegalStateException("订单状态已变化，请重试");
        }

        // Payment and outbox event commit in the same trade database transaction.
        String eventId = UUID.randomUUID().toString();
        String payload =
                "{\"orderNo\":\""
                + order.orderNo()
        + "\",\"paymentNo\":\""
                + notification.paymentNo()
                + "\"}";
        tradeOutboxGateway.append(eventId, "TRADE_ORDER", order.orderNo(), "TRADE_ORDER_PAID", payload);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
