package com.lemnt.mall.adapter.app.promotion.converter;

import com.lemnt.mall.adapter.app.promotion.res.AppPointActivityRes;
import com.lemnt.mall.application.promotion.dto.PointActivityDTO;

/** App 积分商城响应转换。 */
public final class AppPointConverter {
    private AppPointConverter() {}

    public static AppPointActivityRes activity(PointActivityDTO dto) {
        return new AppPointActivityRes(dto.id(), dto.spuId(), dto.spuName(), dto.picUrl(), dto.status(),
                dto.stock(), dto.totalStock(), dto.point(), dto.price(), dto.marketPrice(),
                dto.products().stream().map(item -> new AppPointActivityRes.Product(item.skuId(),
                        item.exchangeCount(), item.point(), item.price(), item.stock())).toList());
    }
}
