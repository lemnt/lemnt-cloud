package com.lemnt.trade.application.aftersale.qry;

import java.io.Serializable;

/** 会员本人售后分页查询。 */
public record MemberAfterSalePageQry(
        String orderNo,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {
}
