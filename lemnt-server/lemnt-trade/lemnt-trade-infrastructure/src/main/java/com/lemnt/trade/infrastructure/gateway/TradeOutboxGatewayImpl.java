package com.lemnt.trade.infrastructure.gateway;

import com.lemnt.trade.domain.gateway.TradeOutboxGateway;
import com.lemnt.trade.infrastructure.dataobject.TradeOutboxDO;
import com.lemnt.trade.infrastructure.mapper.TradeOutboxMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
}