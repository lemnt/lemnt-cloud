package com.lemnt.product.domain.event.model;

/**
 * 已领取的投递任务，执行版本用于阻止过期线程覆盖新租约。
 */
public record OutboxMessage(
    long id,
    String eventNo,
    String eventType,
    String businessKey,
    String payload,
    int attempts,
    long executeVersion) {
}
