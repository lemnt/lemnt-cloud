package com.lemnt.trade.application.checkout.service;

import com.lemnt.trade.domain.checkout.gateway.CheckoutGateway;
import com.lemnt.trade.domain.gateway.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CheckoutLifecycleService {
    private final TradeOrderGateway orders;
    private final TradeOutboxGateway outbox;
    private final CheckoutGateway checkout;

    @Transactional(rollbackFor = Exception.class)
    public void reservationSucceeded(String orderNo) {
        var order = orders.findByOrderNoForUpdate(orderNo);
        if (order == null) throw new IllegalArgumentException("订单不存在");
        if (!"RESERVING".equals(order.status())) return;
        if (!orders.transitionStatus(orderNo, "RESERVING", "PENDING_PAYMENT"))
            throw new IllegalStateException("订单状态冲突");
        checkout.consumeCart(orderNo, order.memberId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void reservationFailed(String orderNo) {
        var order = orders.findByOrderNoForUpdate(orderNo);
        if (order == null) throw new IllegalArgumentException("订单不存在");
        if (!"RESERVING".equals(order.status())) return;
        if (!orders.transitionStatus(orderNo, "RESERVING", "CLOSED"))
            throw new IllegalStateException("订单状态冲突");
        checkout.releaseCoupon(orderNo);
        outbox.append(
                UUID.randomUUID().toString(), "TRADE_ORDER", orderNo, "TRADE_ORDER_CLOSED", "{}");
    }
}
