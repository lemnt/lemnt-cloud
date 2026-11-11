package com.lemnt.order.application.order.service;

import com.lemnt.order.domain.event.gateway.OrderEventGateway;
import com.lemnt.order.domain.order.gateway.OrderRollbackGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 原调用方兼容适配，补偿统一登记原ORDER_CLOSED事件。
 */
@Component
@RequiredArgsConstructor
public class OrderRollbackPublisher implements OrderRollbackGateway {
    private final OrderPersistenceService orders;
    private final OrderEventGateway events;

    @Transactional(rollbackFor = Exception.class)
    public void publish(String no) {
        var o = orders.findByOrderNoForUpdate(no);
        if (o == null || !"CLOSED".equals(o.status())) throw new IllegalStateException("订单未关闭");
        events.closed(o);
    }
}
