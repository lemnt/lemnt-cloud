package com.lemnt.trade.application.delivery.dto;

import java.math.BigDecimal;
import java.util.List;

/** 运费子规则传输对象。 */
public record DeliveryExpressTemplateChargeDTO(
        Long id,
        Long templateId,
        List<Integer> areaIds,
        Integer chargeMode,
        BigDecimal startCount,
        Integer startPrice,
        BigDecimal extraCount,
        Integer extraPrice
) implements java.io.Serializable {
}
