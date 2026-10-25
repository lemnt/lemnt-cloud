package com.lemnt.trade.domain.model;

public record OutboxEvent(String eventId, String aggregateNo, String eventType,
                          String payload, int retryCount) {}
