package com.lemnt.trade.domain.gateway;

import com.lemnt.trade.domain.model.OutboxEvent;
import java.util.List;

public interface TradeOutboxGateway {
    void append(String eventId, String aggregateType, String aggregateNo, String eventType, String payload);
    List<OutboxEvent> pending(int limit);
    boolean claim(String eventId, String leaseToken);
    boolean delivered(String eventId, String leaseToken);
    boolean failed(String eventId, String leaseToken, int attempt);
    boolean replayDead(String eventId);
}
