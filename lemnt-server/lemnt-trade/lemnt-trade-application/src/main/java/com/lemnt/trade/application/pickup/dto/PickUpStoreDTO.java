package com.lemnt.trade.application.pickup.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 自提门店输出。 */
public record PickUpStoreDTO(
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
        List<Long> verifyUserIds,
        Integer status)
        implements Serializable {
    public PickUpStoreDTO {
        verifyUserIds = verifyUserIds == null ? List.of() : List.copyOf(verifyUserIds);
    }
}
