package com.lemnt.trade.infrastructure.order.gateway;

import com.lemnt.common.mq.producer.BaseProducer;
import com.lemnt.trade.domain.order.gateway.OrderRollbackGateway;
import com.lemnt.trade.domain.order.model.OrderRollbackEvent;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 订单补偿使用项目既有 RocketMQ 封装，不再维护本地消息表。 */
@Component
@RequiredArgsConstructor
public class OrderRollbackGatewayImpl implements OrderRollbackGateway {
    private final BaseProducer<OrderRollbackEvent> producer;

    @Override
    public void publish(String orderNo) {
        producer.sendOrderly(OrderRollbackEvent.TOPIC, new OrderRollbackEvent(orderNo), orderNo);
    }
}
