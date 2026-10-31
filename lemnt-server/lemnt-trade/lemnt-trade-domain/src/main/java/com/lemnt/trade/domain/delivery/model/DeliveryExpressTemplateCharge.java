package com.lemnt.trade.domain.delivery.model;

import java.math.BigDecimal;
import java.util.List;

/** trade_delivery_express_template_charge 业务对象，金额以分为单位。 */
public record DeliveryExpressTemplateCharge(
        Long id,
        Long templateId,
        List<Integer> areaIds,
        Integer chargeMode,
        BigDecimal startCount,
        Integer startPrice,
        BigDecimal extraCount,
        Integer extraPrice
) {
}
