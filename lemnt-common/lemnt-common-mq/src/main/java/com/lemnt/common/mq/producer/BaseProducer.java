package com.lemnt.common.mq.producer;

import com.lemnt.common.core.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author wangxx
 * @version 1.0
 * @date 2025-11-08 11:04
 */
@Slf4j
@Component
public class BaseProducer<T> {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 同步发送
     *
     * @param topic
     * @param payload
     */
    public void sendSync(String topic, T payload) {
        rocketMQTemplate.convertAndSend(topic, JsonUtils.toJsonStr(payload));
        log.debug("Sync message sent to topic={} payload={}", topic, JsonUtils.toJsonStr(payload));
    }

    /**
     * 同步发送
     *
     * @param topic
     * @param payloadList
     */
    public void sendListSync(String topic, List<T> payloadList) {
        rocketMQTemplate.convertAndSend(topic, JsonUtils.toJsonStr(payloadList));
        log.debug("Sync message sent to topic={} payload={}", topic, JsonUtils.toJsonStr(payloadList));
    }

    /**
     * 异步发送
     *
     * @param topic
     * @param payload
     */
    public void sendAsync(String topic, T payload) {
        rocketMQTemplate.syncSend(topic, MessageBuilder.withPayload(JsonUtils.toJsonStr(payload)).build());
        log.debug("Async message sending to topic={} payload={}", topic, JsonUtils.toJsonStr(payload));
    }

    /**
     * 异步发送延时消息
     *
     * @param topic
     * @param payload
     */
    public void sendSyncDelay(String topic, T payload, long delayTime) {
        rocketMQTemplate.syncSendDelayTimeMills(topic, MessageBuilder.withPayload(JsonUtils.toJsonStr(payload)).build(), delayTime);
        log.debug("syncSendDeliverTimeMills message sending to topic={} payload={} delayTime={}", topic, JsonUtils.toJsonStr(payload), delayTime);
    }

    /**
     * 异步发送
     *
     * @param topic
     * @param payloadList
     * @param callback
     */
    public void sendListAsync(String topic, List<T> payloadList, SendCallback callback) {
        rocketMQTemplate.asyncSend(topic, MessageBuilder.withPayload(JsonUtils.toJsonStr(payloadList)).build(), callback);
        log.debug("Async message sending to topic={} payload={}", topic, JsonUtils.toJsonStr(payloadList));
    }

    /**
     * 顺序发送
     *
     * @param topic
     * @param payload
     * @param hashKey
     */
    public void sendOrderly(String topic, T payload, String hashKey) {
        rocketMQTemplate.syncSendOrderly(topic, MessageBuilder.withPayload(JsonUtils.toJsonStr(payload)).build(), hashKey);
        log.debug("Orderly message sent to topic={} hashKey={} payload={}", topic, hashKey, JsonUtils.toJsonStr(payload));
    }

    /**
     * 顺序发送
     *
     * @param topic
     * @param payloadList
     * @param hashKey
     */
    public void sendListOrderly(String topic, List<T> payloadList, String hashKey) {
        rocketMQTemplate.syncSendOrderly(topic, MessageBuilder.withPayload(JsonUtils.toJsonStr(payloadList)).build(), hashKey);
        log.debug("Orderly message sent to topic={} hashKey={} payload={}", topic, hashKey, JsonUtils.toJsonStr(payloadList));
    }

    /**
     * 带 Tag 发送
     *
     * @param topic
     * @param tag
     * @param payload
     */
    public void sendWithTag(String topic, String tag, T payload) {
        rocketMQTemplate.convertAndSend(topic + ":" + tag, JsonUtils.toJsonStr(payload));
        log.debug("Message sent to topic={} tag={} payload={}", topic, tag, JsonUtils.toJsonStr(payload));
    }

    /**
     * 带 Tag 发送
     *
     * @param topic
     * @param tag
     * @param payloadList
     */
    public void sendListWithTag(String topic, String tag, List<T> payloadList) {
        rocketMQTemplate.convertAndSend(topic + ":" + tag, JsonUtils.toJsonStr(payloadList));
        log.debug("Message sent to topic={} tag={} payload={}", topic, tag, JsonUtils.toJsonStr(payloadList));
    }

}
