package com.lemnt.order.domain.brokerage.model;

/** 分销用户分页条件。 */
public record BrokerageUserFilter(
        Long bindUserId, Boolean brokerageEnabled, int pageNum, int pageSize) {}
