package com.lemnt.common.client.event;

import java.io.Serializable;
import java.time.Instant;

/** 跨服务事件信封；payload为版本化JSON，原事件号及发生时间在重投时保持不变。 */
public record CommerceEventEnvelope(
        String eventNo,
        String eventType,
        int schemaVersion,
        String businessKey,
        String aggregateNo,
        Instant occurredAt,
        String traceId,
        String payload)
        implements Serializable {
    /** 不允许未知协议被消费者按默认值降级处理。 */
    public void requireType(String expectedType) {
        if (schemaVersion != 1
                || !expectedType.equals(eventType)
                || eventNo == null
                || eventNo.isBlank()
                || businessKey == null
                || businessKey.isBlank()
                || aggregateNo == null
                || aggregateNo.isBlank()
                || occurredAt == null
                || payload == null
                || payload.isBlank()) {
            throw new IllegalArgumentException("事件协议版本、类型或必要字段不合法");
        }
    }
}
