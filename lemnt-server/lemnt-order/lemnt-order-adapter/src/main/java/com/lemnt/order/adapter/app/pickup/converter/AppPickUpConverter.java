package com.lemnt.order.adapter.app.pickup.converter;

import com.lemnt.order.adapter.app.pickup.res.AppPickUpStoreRes;
import com.lemnt.order.application.pickup.dto.AppPickUpStoreDTO;

/**
 * 小程序自提门店转换。
 */
public final class AppPickUpConverter {
    private AppPickUpConverter() {
    }

    public static AppPickUpStoreRes store(AppPickUpStoreDTO value) {
        return new AppPickUpStoreRes(
            value.id(),
            value.name(),
            value.introduction(),
            value.phone(),
            value.areaId(),
            value.detailAddress(),
            value.logo(),
            value.openingTime(),
            value.closingTime(),
            value.latitude(),
            value.longitude(),
            value.distanceKm());
    }
}
