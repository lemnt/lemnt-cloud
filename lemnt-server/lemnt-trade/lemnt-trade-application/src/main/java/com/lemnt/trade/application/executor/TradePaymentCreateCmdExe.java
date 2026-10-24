package com.lemnt.trade.application.executor;

import com.lemnt.trade.domain.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.model.PaymentRecord;
import com.lemnt.trade.domain.model.TradeOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TradePaymentCreateCmdExe {
    private static final Set<String> SUPPORTED_CHANNELS = Set.of("ALIPAY", "WECHAT");
    private final TradeOrderGateway orders;
    private final TradePaymentGateway payments;

    @Transactional(rollbackFor = Exception.class)
    public PaymentRecord execute(String orderNo, String channel) {
        if (orderNo == null || orderNo.isBlank() || channel == null || channel.isBlank()) {
            throw new IllegalArgumentException("订单号和支付渠道不能为空");
        }
        String normalizedChannel = channel.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_CHANNELS.contains(normalizedChannel)) {
            throw new IllegalArgumentException("暂不支持该支付渠道");
        }
        TradeOrder order = orders.findByOrderNo(orderNo.trim());
        if (order == null) throw new IllegalArgumentException("订单不存在");
        if (!"PENDING_PAYMENT".equals(order.status())) {
            throw new IllegalStateException("只有待支付订单可以创建支付单");
        }

        PaymentRecord existing = payments.findByOrderNoAndChannel(order.orderNo(), normalizedChannel);
        if (existing != null) return existing;
        String paymentNo = "PAY" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 24).toUpperCase(Locale.ROOT);
        return payments.createWaiting(paymentNo, order.orderNo(), normalizedChannel, order.totalAmount());
    }
}