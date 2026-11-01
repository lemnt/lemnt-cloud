package com.lemnt.trade.domain.delivery.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 自提门店：verifyUserIds 为可核销的管理员编号。 */
public record PickUpStore(
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
        Integer status
) implements Serializable {
    public PickUpStore {
        verifyUserIds = verifyUserIds == null ? List.of() : List.copyOf(verifyUserIds);
    }
}
