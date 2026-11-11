package com.lemnt.order.domain.delivery.model;

/**
 * order_delivery_express_template 业务对象，金额以分为单位。
 */
public record DeliveryExpressTemplate(Long id, String name, Integer chargeMode, Integer sort) {
}
