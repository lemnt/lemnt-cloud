package com.lemnt.order.application.delivery.dto;

import java.io.Serializable;

/**
 * 配送主体传输对象。
 */
public record DeliveryExpressTemplateDTO(Long id, String name, Integer chargeMode, Integer sort)
    implements Serializable {
}
