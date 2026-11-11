package com.lemnt.order.adapter.mq;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.application.event.service.PaySuccessEventHandler;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 消费失败原样抛给broker，幂等凭证只在本地业务成功时提交。
 */
@Component
@ConditionalOnProperty(name = "commerce.mq.enabled", havingValue = "true")
@RequiredArgsConstructor
@RocketMQMessageListener(
    topic = CommerceEventTopics.PAY_SUCCESS,
    consumerGroup = "lemnt-order-pay-success-v1",
    maxReconsumeTimes = 16)
public class PaySuccessConsumer implements RocketMQListener<String> {
    private final PaySuccessEventHandler handler;

    @Override
    public void onMessage(String message) {
        var envelope = JsonUtils.toBean(message, CommerceEventEnvelope.class);
        if (envelope == null) throw new IllegalArgumentException("事件信封不能为空");
        handler.execute(envelope);
    }
}
