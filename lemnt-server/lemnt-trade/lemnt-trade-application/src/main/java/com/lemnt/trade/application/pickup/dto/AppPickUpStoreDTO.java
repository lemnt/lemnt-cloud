package com.lemnt.trade.application.pickup.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/** 小程序自提门店展示，不暴露核销店员。 */
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
        BigDecimal longitude
) implements Serializable {
}
