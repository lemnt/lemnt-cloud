package com.lemnt.promotion.adapter.app.promotion.converter;

import com.lemnt.promotion.adapter.app.promotion.res.AppBannerRes;
import com.lemnt.promotion.application.promotion.dto.AppBannerDTO;

/** 小程序 Banner 转换。 */
public final class AppBannerConverter {
    private AppBannerConverter() {}

    public static AppBannerRes banner(AppBannerDTO dto) {
        return new AppBannerRes(dto.id(), dto.title(), dto.url(), dto.picUrl());
    }
}
