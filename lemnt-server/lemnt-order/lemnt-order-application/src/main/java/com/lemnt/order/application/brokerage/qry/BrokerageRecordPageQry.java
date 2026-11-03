package com.lemnt.order.application.brokerage.qry;

import java.io.Serializable;

/** 佣金记录分页查询。 */
public record BrokerageRecordPageQry(
        Long userId, Integer bizType, Integer status, int pageNum, int pageSize)
        implements Serializable {}
