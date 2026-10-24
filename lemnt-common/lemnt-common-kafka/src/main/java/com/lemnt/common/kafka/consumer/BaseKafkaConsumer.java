package com.lemnt.common.kafka.consumer;

import lombok.extern.slf4j.Slf4j;

/**
 * kafka消息消费者抽象类
 *
 * @author wangxx
 */
@Slf4j
public abstract class BaseKafkaConsumer<T> {

    protected abstract void onMessage(T message);

    protected void onError(Exception e, T message) {
        log.error("Kafka consume error: {}", message, e);
    }

    protected void handle(T message) {
        try {
            onMessage(message);
        } catch (Exception e) {
            onError(e, message);
        }
    }
}
