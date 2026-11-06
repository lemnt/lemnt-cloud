package com.lemnt.member.adapter.mq;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.member.application.event.service.OrderBenefitsEventHandler;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 失败原样抛给Broker重试，业务与消费凭证不能提前提交。
 */
@Component
@ConditionalOnProperty(name = "commerce.mq.enabled", havingValue = "true")
@RequiredArgsConstructor
@RocketMQMessageListener(
    topic = CommerceEventTopics.ORDER_PAID,
    consumerGroup = "lemnt-member-paid-v1",
    maxReconsumeTimes = 16)
public class OrderPaidConsumer implements RocketMQListener<String> {
    private final OrderBenefitsEventHandler handler;

    public void onMessage(String json) {
        var e = JsonUtils.toBean(json, CommerceEventEnvelope.class);
        if (e == null) throw new IllegalArgumentException("事件信封不能为空");
        handler.paid(e);
    }
}
