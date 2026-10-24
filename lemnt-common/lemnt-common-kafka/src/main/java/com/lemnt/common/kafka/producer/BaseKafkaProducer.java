package com.lemnt.common.kafka.producer;

import com.lemnt.common.kafka.model.KafkaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka 消息生产者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BaseKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 消息推送
     */
    public <T> void send(String topic, String key, T data) {
        kafkaTemplate.send(topic, key, data).whenComplete(
                (result, ex) -> {
                    if (ex == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Kafka send success topic={}, key={}", topic, key);
                        }
                    } else {
                        log.error("Kafka send failed topic={}, key={}", topic, key, ex);
                    }
                }
        );
    }

    /**
     * 消息推送
     */
    public <T> void send(KafkaMessage<T> message) {
        kafkaTemplate.send(message.getTopic(), message.getKey(), message).whenComplete(
                (result, ex) -> {
                    if (ex == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Kafka send success topic={}, key={}", message.getTopic(), message.getKey());
                        }
                    } else {
                        log.error("Kafka send failed topic={}, key={}", message.getTopic(), message.getKey(), ex);
                    }
                }
        );
    }
}