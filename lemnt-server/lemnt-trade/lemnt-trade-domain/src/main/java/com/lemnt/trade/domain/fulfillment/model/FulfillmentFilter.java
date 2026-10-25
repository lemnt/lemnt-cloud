package com.lemnt.trade.domain.fulfillment.model;

public record FulfillmentFilter(String orderNo, String status, int offset, int limit) {}
