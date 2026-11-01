package com.lemnt.trade.application.pickup.cmd;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 自提门店新增/修改命令，id 为空表示新增。 */
public record PickUpStoreSaveCmd(
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
}
