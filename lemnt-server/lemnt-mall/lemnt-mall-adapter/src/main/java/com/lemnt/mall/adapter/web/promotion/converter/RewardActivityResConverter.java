package com.lemnt.mall.adapter.web.promotion.converter;

import com.lemnt.mall.adapter.web.promotion.res.RewardActivityDetailRes;
import com.lemnt.mall.adapter.web.promotion.res.RewardActivityPageRes;
import com.lemnt.mall.adapter.web.promotion.res.RewardRuleRes;
import com.lemnt.mall.application.promotion.dto.RewardActivityDTO;

/** 活动响应转换。 */
public final class RewardActivityResConverter {
    private RewardActivityResConverter() {}

    public static RewardActivityDetailRes detail(RewardActivityDTO v) {
        return new RewardActivityDetailRes(
                v.id(),
                v.name(),
                v.status(),
                v.startTime(),
                v.endTime(),
                v.remark(),
                v.conditionType(),
                v.productScope(),
                v.productScopeValues(),
                v.rules().stream().map(r -> new RewardRuleRes(r.limit(), r.discountPrice())).toList());
    }

    public static RewardActivityPageRes page(RewardActivityDTO v) {
        return new RewardActivityPageRes(v.id(), v.name(), v.status(), v.startTime(), v.endTime(), v.remark());
    }
}
