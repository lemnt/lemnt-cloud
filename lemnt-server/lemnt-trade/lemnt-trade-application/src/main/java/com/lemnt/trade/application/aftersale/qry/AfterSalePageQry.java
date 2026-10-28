package com.lemnt.trade.application.aftersale.qry;

import java.io.Serializable;

/** 售后分页查询。 */
public record AfterSalePageQry(
        String orderNo,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {
}
