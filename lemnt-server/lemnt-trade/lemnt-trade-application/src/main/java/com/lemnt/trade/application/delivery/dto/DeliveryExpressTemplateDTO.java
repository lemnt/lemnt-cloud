package com.lemnt.trade.application.delivery.dto;

/** 配送主体传输对象。 */
public record DeliveryExpressTemplateDTO(
        Long id,
        String name,
        Integer chargeMode,
        Integer sort
) implements java.io.Serializable {
}
