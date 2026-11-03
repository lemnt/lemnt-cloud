package com.lemnt.promotion.adapter.mq;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.promotion.application.event.service.OrderClosedEventHandler;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 消费失败原样抛给broker，幂等凭证只在本地业务成功时提交。 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = CommerceEventTopics.ORDER_CLOSED,
        consumerGroup = "lemnt-promotion-order-closed-v1",
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
