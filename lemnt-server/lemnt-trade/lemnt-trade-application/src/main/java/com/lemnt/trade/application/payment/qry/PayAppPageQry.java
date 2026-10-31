package com.lemnt.trade.application.payment.qry;

import java.io.Serializable;

/** 支付应用分页查询。 */
public record PayAppPageQry(
        String appKey,
        String name,
        Integer status,
        int pageNum,
        int pageSize
) implements Serializable {
}
