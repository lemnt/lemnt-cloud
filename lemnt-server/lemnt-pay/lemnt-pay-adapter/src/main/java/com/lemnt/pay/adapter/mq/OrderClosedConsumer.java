package com.lemnt.pay.adapter.mq;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.pay.application.event.service.OrderClosedEventHandler;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 失败直接抛给broker重试，不能继承吞异常的旧BaseConsumer。 */
@Component
@ConditionalOnProperty(name = "commerce.mq.enabled", havingValue = "true")
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = CommerceEventTopics.ORDER_CLOSED,
        consumerGroup = "lemnt-pay-order-closed-v1",
        maxReconsumeTimes = 16)
public class OrderClosedConsumer implements RocketMQListener<String> {
    private final OrderClosedEventHandler handler;

    @Override
    public void onMessage(String message) {
        var envelope = JsonUtils.toBean(message, CommerceEventEnvelope.class);
        if (envelope == null) throw new IllegalArgumentException("事件信封不能为空");
        handler.execute(envelope);
    }
}
