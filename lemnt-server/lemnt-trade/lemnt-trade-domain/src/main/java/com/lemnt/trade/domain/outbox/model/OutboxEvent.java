package com.lemnt.trade.domain.outbox.model;

/**
 * 事务消息事件。
 */
public record OutboxEvent(
        String eventId,
        String aggregateNo,
        String eventType,
        String payload,
        int retryCount
) {}
