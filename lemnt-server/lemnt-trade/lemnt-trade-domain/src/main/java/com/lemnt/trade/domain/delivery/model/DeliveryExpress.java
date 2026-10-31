package com.lemnt.trade.domain.delivery.model;

/** trade_delivery_express 业务对象，金额以分为单位。 */
public record DeliveryExpress(
        Long id,
        String code,
        String name,
        String logo,
        Integer sort,
        Integer status
) {
}
