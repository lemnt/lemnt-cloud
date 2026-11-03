package com.lemnt.trade.infrastructure.delivery.converter;

import com.lemnt.trade.domain.delivery.model.PickUpStore;
import com.lemnt.trade.infrastructure.delivery.dataobject.PickUpStoreDO;

import com.alibaba.fastjson2.JSON;

import java.util.List;

/** 自提门店领域对象与表结构转换。 */
public final class PickUpStoreConverter {
    private PickUpStoreConverter() {}

    public static PickUpStore model(PickUpStoreDO row) {
        if (row == null) {
            return null;
        }
        List<Long> verifyUserIds =
                row.getVerifyUserIds() == null || row.getVerifyUserIds().isBlank()
                        ? List.of()
                        : JSON.parseArray(row.getVerifyUserIds(), Long.class);
        return new PickUpStore(
                row.getId(),
                row.getName(),
                row.getIntroduction(),
                row.getPhone(),
                row.getAreaId(),
                row.getDetailAddress(),
                row.getLogo(),
                row.getOpeningTime(),
                row.getClosingTime(),
                row.getLatitude(),
                row.getLongitude(),
                verifyUserIds,
                row.getStatus());
    }

    public static PickUpStoreDO row(PickUpStore value) {
        var row = new PickUpStoreDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setIntroduction(value.introduction());
        row.setPhone(value.phone());
        row.setAreaId(value.areaId());
        row.setDetailAddress(value.detailAddress());
        row.setLogo(value.logo());
        row.setOpeningTime(value.openingTime());
        row.setClosingTime(value.closingTime());
        row.setLatitude(value.latitude());
        row.setLongitude(value.longitude());
        row.setVerifyUserIds(JSON.toJSONString(value.verifyUserIds()));
        row.setStatus(value.status());
        return row;
    }
}
