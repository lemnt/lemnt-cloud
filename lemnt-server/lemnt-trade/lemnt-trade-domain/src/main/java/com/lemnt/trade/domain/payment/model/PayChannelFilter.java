package com.lemnt.trade.domain.payment.model;

import java.io.Serializable;

/** 支付渠道配置分页筛选。 */
public record PayChannelFilter(
        Long appId,
        String code,
        Integer status,
        int pageNum,
        int pageSize
) implements Serializable {
}
