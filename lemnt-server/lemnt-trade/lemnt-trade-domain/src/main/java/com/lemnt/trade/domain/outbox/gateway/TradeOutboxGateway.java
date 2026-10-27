package com.lemnt.trade.domain.outbox.gateway;

import com.lemnt.trade.domain.outbox.model.OutboxEvent;

import java.util.List;

/**
 * 交易事务消息领域网关。
 */
public interface TradeOutboxGateway {
    /**
     * 追加事务消息。
     */
    void append(String eventId, String aggregateType, String aggregateNo, String eventType, String payload);

    /**
     * 待处理。
     */
    List<OutboxEvent> pending(int limit);

    /**
     * 领取。
     */
    boolean claim(String eventId, String leaseToken);

    /**
     * 已投递。
     */
    boolean delivered(String eventId, String leaseToken);

    /**
     * 失败。
     */
    boolean failed(String eventId, String leaseToken, int attempt);

    /**
     * 重放失效。
     */
    boolean replayDead(String eventId);
}
