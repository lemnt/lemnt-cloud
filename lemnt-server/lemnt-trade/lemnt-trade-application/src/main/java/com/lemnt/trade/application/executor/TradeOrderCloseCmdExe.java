package com.lemnt.trade.application.executor;

import com.lemnt.trade.domain.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.gateway.PaymentChannelCloseGateway;
import com.lemnt.trade.domain.gateway.TradeOutboxGateway;
import com.lemnt.trade.domain.model.TradeOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TradeOrderCloseCmdExe {
    private final TradeOrderGateway orders;
    private final TradeOutboxGateway outbox;
    private final TradePaymentGateway payments;
    private final PaymentChannelCloseGateway channels;
    private final com.lemnt.trade.domain.checkout.gateway.CheckoutGateway checkout;

    /**
     * Close the order and durably record the inventory release in one local transaction. No remote
     * inventory call is made before the transaction commits.
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) throw new IllegalArgumentException("订单号不能为空");
        TradeOrder order = orders.findByOrderNoForUpdate(orderNo);
        if (order == null) throw new IllegalArgumentException("订单不存在");
        if ("CLOSED".equals(order.status())) return true;
        if (!java.util.Set.of("PENDING_PAYMENT", "RESERVING").contains(order.status()))
            return false;
        for (var payment : payments.findByOrderNo(orderNo)) {
            if ("CLOSED".equals(payment.status())) continue;
            if (!"WAITING".equals(payment.status()) || !channels.closeUnpaid(payment)) return false;
            if (!payments.markClosed(payment.paymentNo())) {
                throw new IllegalStateException("支付单状态变化，请重试关单");
            }
        }
        if (!orders.transitionStatus(orderNo, order.status(), "CLOSED")) return false;
        checkout.releaseCoupon(orderNo);
        outbox.append(
                UUID.randomUUID().toString(),
                "TRADE_ORDER",
                orderNo,
                "TRADE_ORDER_CLOSED",
                "{\"orderNo\":\"" + orderNo + "\"}");
        return true;
    }
}
