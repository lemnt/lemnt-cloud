package com.lemnt.order.application.delivery.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 运费子规则传输对象。
 */
public record DeliveryExpressTemplateFreeDTO(
    Long id, Long templateId, List<Integer> areaIds, Integer freePrice, Integer freeCount)
    implements Serializable {
}
