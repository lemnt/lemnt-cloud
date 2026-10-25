package com.lemnt.trade.infrastructure.gateway;

import com.lemnt.trade.domain.gateway.TradeOutboxGateway;
import com.lemnt.trade.domain.model.OutboxEvent;
import com.lemnt.trade.infrastructure.dataobject.TradeOutboxDO;
import com.lemnt.trade.infrastructure.mapper.TradeOutboxMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TradeOutboxGatewayImpl implements TradeOutboxGateway {
    private final TradeOutboxMapper mapper;

    @Override
    public void append(String eventId, String aggregateType, String aggregateNo, String eventType, String payload) {
        mapper.insert(new TradeOutboxDO().setEventId(eventId).setAggregateType(aggregateType)
                .setAggregateNo(aggregateNo).setEventType(eventType).setPayload(payload)
                .setStatus("NEW").setRetryCount(0));
    }

    @Override
    public List<OutboxEvent> pending(int limit) {
        int batch = Math.max(1, Math.min(100, limit));
        mapper.exhaust();
        return mapper.pending(batch)
                .stream().map(row -> new OutboxEvent(row.getEventId(), row.getAggregateNo(),
                        row.getEventType(), row.getPayload(), row.getRetryCount())).toList();
    }

    @Override
    public boolean claim(String id, String token) {
        return mapper.claim(id, token) == 1;
    }

    @Override
    public boolean delivered(String id, String token) {
        return mapper.delivered(id, token) == 1;
    }

    @Override
    public boolean failed(String id, String token, int attempt) {
        long delay = Math.min(3600L, 30L << Math.max(0, Math.min(7, attempt - 1)));
        return mapper.failed(id, token, delay) == 1;
    }

    @Override
    public boolean replayDead(String id) {
        return mapper.replayDead(id) == 1;
    }
}
