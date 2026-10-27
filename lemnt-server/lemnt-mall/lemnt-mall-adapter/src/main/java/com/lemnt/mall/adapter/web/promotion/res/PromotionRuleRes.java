package com.lemnt.mall.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 营销规则响应。
 */
@Schema(
        description = "促销规则",
        oneOf = {
    PromotionFullReductionRes.class, PromotionOrderDiscountRes.class
})
public sealed interface PromotionRuleRes extends Serializable
permits PromotionFullReductionRes, PromotionOrderDiscountRes {}
