package com.lemnt.order.application.brokerage.qry;

import java.io.Serializable;

/** 提现单分页查询。 */
public record BrokerageWithdrawPageQry(Long userId, Integer status, int pageNum, int pageSize)
        implements Serializable {}
