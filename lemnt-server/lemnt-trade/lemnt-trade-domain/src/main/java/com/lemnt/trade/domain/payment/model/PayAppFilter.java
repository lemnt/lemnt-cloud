package com.lemnt.trade.domain.payment.model;

import java.io.Serializable;

/** 支付应用分页筛选。 */
public record PayAppFilter(
        String appKey,
        String name,
        Integer status,
        int pageNum,
        int pageSize
) implements Serializable {
}
