package com.lemnt.mall.adapter.web.promotion.converter;

import com.lemnt.mall.adapter.web.promotion.res.DiscountActivityDetailRes;
import com.lemnt.mall.adapter.web.promotion.res.DiscountActivityPageRes;
import com.lemnt.mall.adapter.web.promotion.res.DiscountProductRes;
import com.lemnt.mall.application.promotion.dto.DiscountActivityDTO;

/** 活动响应转换。 */
public final class DiscountActivityResConverter {
    private DiscountActivityResConverter() {}

    public static DiscountActivityDetailRes detail(DiscountActivityDTO v) {
        return new DiscountActivityDetailRes(
                v.id(),
                v.name(),
                v.status(),
                v.startTime(),
                v.endTime(),
                v.remark(),
                v.products().stream().map(p -> new DiscountProductRes(p.spuId(), p.skuId(), p.discountType(),
                    p.discountPercent(), p.discountPrice())).toList());
    }

    public static DiscountActivityPageRes page(DiscountActivityDTO v) {
        return new DiscountActivityPageRes(v.id(), v.name(), v.status(), v.startTime(), v.endTime(), v.remark());
    }
}
