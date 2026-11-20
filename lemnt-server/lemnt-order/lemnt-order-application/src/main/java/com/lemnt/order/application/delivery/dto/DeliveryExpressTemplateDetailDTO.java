package com.lemnt.order.application.delivery.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 运费模板详情。
 */
public record DeliveryExpressTemplateDetailDTO(
    Long id,
    String name,
    Integer chargeMode,
    Integer sort,
    List<DeliveryExpressTemplateChargeDTO> charges,
    List<DeliveryExpressTemplateFreeDTO> frees)
    implements Serializable {
}
