package com.lemnt.order.application.brokerage.qry;

import java.io.Serializable;

/** 分销用户分页查询。 */
public record BrokerageUserPageQry(
        Long bindUserId, Boolean brokerageEnabled, int pageNum, int pageSize)
        implements Serializable {}
