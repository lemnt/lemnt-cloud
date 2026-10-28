package com.lemnt.trade.application.delivery.qry;

/** 配送管理分页查询。 */
public record DeliveryExpressPageQry(
        String name,
        Integer status,
        Integer pageNum,
        Integer pageSize
) implements java.io.Serializable {
}
