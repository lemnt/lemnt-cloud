package com.lemnt.trade.application.delivery.qry;

/** 配送管理分页查询。 */
public record DeliveryExpressTemplatePageQry(
        String name,
        Integer pageNum,
        Integer pageSize
) implements java.io.Serializable {
}
