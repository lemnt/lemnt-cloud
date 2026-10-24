package com.lemnt.trade.domain.gateway;

public interface TradeOutboxGateway {
    void append(String eventId, String aggregateType, String aggregateNo, String eventType, String payload);
}