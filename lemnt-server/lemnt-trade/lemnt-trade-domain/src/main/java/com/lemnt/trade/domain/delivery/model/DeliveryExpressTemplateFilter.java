package com.lemnt.trade.domain.delivery.model;

/** 配送管理分页条件。 */
public record DeliveryExpressTemplateFilter(
        String name,
        Integer pageNum,
        Integer pageSize
) implements java.io.Serializable {
}
