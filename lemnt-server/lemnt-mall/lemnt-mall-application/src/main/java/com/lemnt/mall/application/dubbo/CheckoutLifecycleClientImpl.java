package com.lemnt.mall.application.dubbo;

import com.lemnt.mall.application.checkout.service.CheckoutService;
import com.lemnt.mall.client.CheckoutLifecycleClient;
import com.lemnt.mall.domain.order.gateway.OrderGateway;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/** 订单事件的商城本地事务，不参与交易库事务。 */
@DubboService
@RequiredArgsConstructor
public class CheckoutLifecycleClientImpl implements CheckoutLifecycleClient {
    private final CheckoutService checkout;
    private final OrderGateway orders;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserved(String orderNo) {
        var order = orders.findByOrderNo(orderNo);
        if (order == null || !Set.of("PENDING_PAYMENT", "PAID", "SHIPPED", "COMPLETED", "CLOSED")
                .contains(order.status())) {
            throw new IllegalStateException("订单尚未完成库存预占");
        }
        if (!"CLOSED".equals(order.status())) {
            checkout.consumeCart(orderNo, order.memberId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paid(String orderNo) {
        var order = orders.findByOrderNo(orderNo);
        if (order == null || !Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status())) {
            throw new IllegalStateException("订单未支付");
        }
        checkout.useCoupon(orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closed(String orderNo) {
        var order = orders.findByOrderNo(orderNo);
        if (order == null || !"CLOSED".equals(order.status())) {
            throw new IllegalStateException("订单未关闭");
        }
        checkout.releaseCoupon(orderNo);
    }
}
