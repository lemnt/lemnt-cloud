package com.lemnt.order.domain.delivery.model;

import java.util.List;

/** order_delivery_express_template_free 业务对象，金额以分为单位。 */
public record DeliveryExpressTemplateFree(
        Long id, Long templateId, List<Integer> areaIds, Integer freePrice, Integer freeCount) {}
