package com.lemnt.trade.domain.brokerage.model;

/** 佣金记录分页条件。 */
public record BrokerageRecordFilter(
        Long userId, Integer bizType, Integer status, int pageNum, int pageSize) {}
