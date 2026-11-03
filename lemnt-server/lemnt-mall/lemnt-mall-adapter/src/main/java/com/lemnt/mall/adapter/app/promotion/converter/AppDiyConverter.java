package com.lemnt.mall.adapter.app.promotion.converter;

import com.lemnt.mall.adapter.app.promotion.res.AppDiyPageRes;
import com.lemnt.mall.adapter.app.promotion.res.AppDiyTemplateRes;
import com.lemnt.mall.application.promotion.dto.AppDiyPageDTO;
import com.lemnt.mall.application.promotion.dto.AppDiyTemplateDTO;

/** 小程序装修响应转换。 */
public final class AppDiyConverter {
    private AppDiyConverter() {}

    public static AppDiyTemplateRes template(AppDiyTemplateDTO value) {
        return value == null
                ? null
                : new AppDiyTemplateRes(
                        value.id(), value.name(), value.property(), value.home(), value.user());
    }

    public static AppDiyPageRes page(AppDiyPageDTO value) {
        return new AppDiyPageRes(value.id(), value.name(), value.property());
    }
}
