package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.payment.model.PaymentRecord;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 交易支付创建命令执行器。
 */
@Component
@RequiredArgsConstructor
public class TradePaymentCreateCmdExe {
    private final OrderPersistenceService orderPersistenceService;
    private static final Set<String> SUPPORTED_CHANNELS = Set.of("ALIPAY", "WECHAT");
    private final TradePaymentGateway tradePaymentGateway;

    /**
     * 执行交易支付创建命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecord execute(String orderNo, String channel) {
        if (orderNo == null || orderNo.isBlank() || channel == null || channel.isBlank()) {
            throw new IllegalArgumentException("订单号和支付渠道不能为空");
        }
        String normalizedChannel = channel.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_CHANNELS.contains(normalizedChannel)) {
            throw new IllegalArgumentException("暂不支持该支付渠道");
        }
        TradeOrder order = orderPersistenceService.findByOrderNoForUpdate(orderNo.trim());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!"PENDING_PAYMENT".equals(order.status())) {
            throw new IllegalStateException("只有待支付订单可以创建支付单");
        }

        PaymentRecord existing = tradePaymentGateway.findByOrderNoAndChannel(order.orderNo(), normalizedChannel);
        if (existing != null) {
            return existing;
        }
        String paymentNo = "PAY" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 24).toUpperCase(Locale.ROOT);
        return tradePaymentGateway.createWaiting(paymentNo, order.orderNo(), normalizedChannel, order.totalAmount());
    }
}
