package com.lemnt.product.infrastructure.event.gateway;

import com.lemnt.product.domain.event.gateway.EventPublishGateway;
import com.lemnt.product.domain.event.model.OutboxMessage;

import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * broker明确接受后才允许本地任务完成；不吞异常、不记录完整载荷。
 */
@Component
@RequiredArgsConstructor
public class EventPublishGatewayImpl implements EventPublishGateway {
    private final ObjectProvider<RocketMQTemplate> templates;

    @Override
    public void publish(OutboxMessage message) {
        var template = templates.getIfAvailable();
        if (template == null) throw new IllegalStateException("RocketMQ发布配置缺失");
        var result = template.syncSend(message.eventType(), message.payload());
        if (result == null || result.getSendStatus() != SendStatus.SEND_OK) {
            throw new IllegalStateException("RocketMQ未确认可靠接收");
        }
    }
}
