package com.lemnt.trade.infrastructure.gateway;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
        return mapper.selectList(Wrappers.lambdaQuery(TradeOutboxDO.class)
                .and(q -> q.eq(TradeOutboxDO::getStatus, "NEW")
                        .or().eq(TradeOutboxDO::getStatus, "FAILED")
                        .or().eq(TradeOutboxDO::getStatus, "PROCESSING"))
                .orderByAsc(TradeOutboxDO::getId).last("LIMIT " + batch))
                .stream().map(row -> new OutboxEvent(row.getEventId(), row.getAggregateNo(),
                        row.getEventType(), row.getPayload(), row.getRetryCount())).toList();
    }

    @Override public boolean claim(String id) { return mapper.claim(id) == 1; }
    @Override public void delivered(String id) {
        if (mapper.delivered(id) != 1) throw new IllegalStateException("Outbox ack failed: " + id);
    }
    @Override public void failed(String id) { mapper.failed(id); }
}
