package com.lemnt.trade.application.payment.qry;

import java.io.Serializable;

/** 支付渠道配置分页查询。 */
public record PayChannelPageQry(
        Long appId,
        String code,
        Integer status,
        int pageNum,
        int pageSize
) implements Serializable {
}
