package com.lemnt.trade.infrastructure.outbox.gateway;

import com.lemnt.trade.domain.outbox.gateway.TradeOutboxGateway;
import com.lemnt.trade.domain.outbox.model.OutboxEvent;
import com.lemnt.trade.infrastructure.outbox.dataobject.TradeOutboxDO;
import com.lemnt.trade.infrastructure.outbox.mapper.TradeOutboxMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 交易事务消息领域网关实现。
 */
@Component
@RequiredArgsConstructor
public class TradeOutboxGatewayImpl implements TradeOutboxGateway {
    private final TradeOutboxMapper tradeOutboxMapper;

    @Override
    public void append(String eventId, String aggregateType, String aggregateNo, String eventType, String payload) {
        tradeOutboxMapper.insert(new TradeOutboxDO().setEventId(eventId).setAggregateType(aggregateType)
                .setAggregateNo(aggregateNo).setEventType(eventType).setPayload(payload)
                .setStatus("NEW").setRetryCount(0));
    }

    @Override
    public List<OutboxEvent> pending(int limit) {
        int batch = Math.max(1, Math.min(100, limit));
        tradeOutboxMapper.exhaust();
        return tradeOutboxMapper.pending(batch)
                .stream().map(row -> new OutboxEvent(row.getEventId(), row.getAggregateNo(),
                row.getEventType(), row.getPayload(), row.getRetryCount())).toList();
    }

    @Override
    public boolean claim(String id, String token) {
        return tradeOutboxMapper.claim(id, token) == 1;
    }

    @Override
    public boolean delivered(String id, String token) {
        return tradeOutboxMapper.delivered(id, token) == 1;
    }

    @Override
    public boolean failed(String id, String token, int attempt) {
        long delay = Math.min(3600L, 30L << Math.max(0, Math.min(7, attempt - 1)));
        return tradeOutboxMapper.failed(id, token, delay) == 1;
    }

    @Override
    public boolean replayDead(String id) {
        return tradeOutboxMapper.replayDead(id) == 1;
    }
}
