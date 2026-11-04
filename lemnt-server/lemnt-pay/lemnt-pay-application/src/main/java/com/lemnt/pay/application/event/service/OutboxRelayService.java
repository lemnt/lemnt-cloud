package com.lemnt.pay.application.event.service;

import com.lemnt.pay.domain.event.gateway.EventPublishGateway;
import com.lemnt.pay.domain.event.gateway.MessageOutboxGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 短事务领取，事务外发送；发送后崩溃由相同事件号重新投递。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelayService {
    private final MessageOutboxGateway outbox;
    private final EventPublishGateway publisher;

    public void relay() {
        for (var message : outbox.claim(100)) {
            try {
                publisher.publish(message);
                if (!outbox.sent(message)) log.warn("消息投递完成时租约已失效，eventNo={}", message.eventNo());
            } catch (Exception failure) {
                outbox.failed(message, failure.getClass().getSimpleName());
                log.error(
                    "消息投递失败，eventNo={}, attempts={}, error={}",
                    message.eventNo(),
                    message.attempts(),
                    failure.getClass().getSimpleName());
            }
        }
    }
}
