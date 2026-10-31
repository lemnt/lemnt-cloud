package com.lemnt.trade.application.delivery.dto;

import java.util.List;

/** 运费模板详情。 */
public record DeliveryExpressTemplateDetailDTO(
        Long id,
        String name,
        Integer chargeMode,
        Integer sort,
        List<DeliveryExpressTemplateChargeDTO> charges,
        List<DeliveryExpressTemplateFreeDTO> frees
) implements java.io.Serializable {
}
