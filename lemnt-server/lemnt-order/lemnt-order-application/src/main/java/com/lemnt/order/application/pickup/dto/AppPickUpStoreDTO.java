package com.lemnt.order.application.pickup.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 小程序自提门店展示，不暴露核销店员。
 */
public record AppPickUpStoreDTO(
    Long id,
    String name,
    String introduction,
    String phone,
    Long areaId,
    String detailAddress,
    String logo,
    String openingTime,
    String closingTime,
    BigDecimal latitude,
    BigDecimal longitude,
    BigDecimal distanceKm)
    implements Serializable {
}
