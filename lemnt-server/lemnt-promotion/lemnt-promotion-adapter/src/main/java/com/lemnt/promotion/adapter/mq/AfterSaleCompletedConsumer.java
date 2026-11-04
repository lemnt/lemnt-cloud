package com.lemnt.promotion.adapter.mq;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.promotion.application.event.service.AfterSaleCompletedEventHandler;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 消费失败原样抛给broker，幂等凭证只在本地业务成功时提交。 */
@Component
@ConditionalOnProperty(name = "commerce.mq.enabled", havingValue = "true")
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = CommerceEventTopics.AFTER_SALE_COMPLETED,
        consumerGroup = "lemnt-promotion-after-sale-completed-v1",
        maxReconsumeTimes = 16)
public class AfterSaleCompletedConsumer implements RocketMQListener<String> {
    private final AfterSaleCompletedEventHandler handler;

    @Override
    public void onMessage(String message) {
        var envelope = JsonUtils.toBean(message, CommerceEventEnvelope.class);
        if (envelope == null) throw new IllegalArgumentException("事件信封不能为空");
        handler.execute(envelope);
    }
}
