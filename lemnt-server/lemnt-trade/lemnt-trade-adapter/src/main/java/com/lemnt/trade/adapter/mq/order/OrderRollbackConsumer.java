package com.lemnt.trade.adapter.mq.order;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.trade.application.order.executor.OrderRollbackCmdExe;
import com.lemnt.trade.domain.order.model.OrderRollbackEvent;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 消费失败直接抛给 RocketMQ，复用其重试机制。 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = OrderRollbackEvent.TOPIC, consumerGroup = OrderRollbackEvent.GROUP,
        consumeMode = ConsumeMode.ORDERLY)
public class OrderRollbackConsumer implements RocketMQListener<String> {
    private final OrderRollbackCmdExe executor;

    @Override
    public void onMessage(String message) {
        var event = JsonUtils.toBean(message, OrderRollbackEvent.class);
        if (event == null || event.orderNo() == null || event.orderNo().isBlank()) {
            throw new IllegalArgumentException("订单补偿事件不合法");
        }
        executor.execute(event.orderNo());
    }
}
