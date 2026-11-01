package com.lemnt.mall.adapter.app.promotion.converter;

import com.lemnt.mall.adapter.app.promotion.res.AppBannerRes;
import com.lemnt.mall.application.promotion.dto.AppBannerDTO;

/** 小程序 Banner 转换。 */
public final class AppBannerConverter {
    private AppBannerConverter() {
    }

    public static AppBannerRes banner(AppBannerDTO dto) {
        return new AppBannerRes(dto.id(), dto.title(), dto.url(), dto.picUrl());
    }
}
