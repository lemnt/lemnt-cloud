package com.lemnt.pay.application.payment.executor;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.pay.domain.channel.model.PaymentChannel;
import com.lemnt.pay.domain.event.gateway.MessageInboxGateway;
import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.order.model.OrderPaymentSnapshot;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PaymentRecord;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * 交易支付创建命令执行器。
 */
@Component
@RequiredArgsConstructor
public class TradePaymentCreateCmdExe {
    private final OrderPaymentGateway orderPersistenceService;
    private final TradePaymentGateway tradePaymentGateway;
    private final MessageInboxGateway receipts;

    /**
     * 执行交易支付创建命令。
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public PaymentRecord execute(String orderNo, String channel) {
        return execute(orderNo, channel, null);
    }

    /**
     * 小程序支付创建时绑定支付应用，回调按应用解析渠道密钥。
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public PaymentRecord execute(String orderNo, String channel, String appKey) {
        if (orderNo == null || orderNo.isBlank() || channel == null || channel.isBlank()) {
            throw new IllegalArgumentException("订单号和支付渠道不能为空");
        }
        String normalizedChannel = channel.trim().toUpperCase(Locale.ROOT);
        try {
            PaymentChannel.valueOf(normalizedChannel);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("暂不支持该支付渠道");
        }
        OrderPaymentSnapshot order = orderPersistenceService.preparePayment(orderNo.trim());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!"PENDING_PAYMENT".equals(order.status())) {
            throw new IllegalStateException("只有待支付订单可以创建支付单");
        }

        if (order.totalAmount() == null || order.totalAmount().signum() <= 0)
            throw new IllegalStateException("零元订单无需创建支付单");

        receipts.begin(
            "pay.order.fence.v1",
            order.orderNo(),
            "PAYMENT_ORDER_FENCE",
            order.orderNo(),
            JsonUtils.toJsonStr(Map.of("orderNo", order.orderNo())));
        if (receipts.has("pay.order.closed.v1", "ORDER_CLOSED", order.orderNo()))
            throw new IllegalStateException("订单已关闭，不能创建延迟支付尝试");

        // 支付准备 RPC 可能早于关单消费。拿到同一支付锁后重查订单的当前状态。
        var current = orderPersistenceService.findByOrderNo(order.orderNo());
        if (current == null
                || !"PENDING_PAYMENT".equals(current.status())
                || !order.orderNo().equals(current.orderNo())
                || order.memberId() == null
                || !order.memberId().equals(current.memberId())
                || current.totalAmount() == null
                || order.totalAmount().compareTo(current.totalAmount()) != 0) {
            throw new IllegalStateException("原订单状态或支付准备金额已变化");
        }

        if (tradePaymentGateway.findByOrderNo(order.orderNo()).stream()
                .anyMatch(attempt -> conflicts(normalizedChannel, attempt))) {
            throw new IllegalStateException("订单已有其他渠道支付，不能重复收款");
        }

        String paymentNo =
            "PAY"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 24)
                .toUpperCase(Locale.ROOT);
        return tradePaymentGateway.createWaiting(
            paymentNo, order.orderNo(), order.memberId(), normalizedChannel, appKey, order.totalAmount());
    }

    private boolean conflicts(String channel, PaymentRecord attempt) {
        String wallet = PaymentChannel.WALLET.name();
        return !channel.equals(attempt.channel())
                && (wallet.equals(channel)
                        || wallet.equals(attempt.channel())
                        || (PaymentChannel.WECHAT.name().equals(channel)
                                && "SUCCESS".equals(attempt.status())))
                && !"CLOSED".equals(attempt.status());
    }
}
