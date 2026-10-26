package com.lemnt.trade.adapter.web.promotion.res;

@io.swagger.v3.oas.annotations.media.Schema(
        description = "促销规则",
        oneOf = {PromotionFullReductionRes.class, PromotionOrderDiscountRes.class})
public sealed interface PromotionRuleRes extends java.io.Serializable
        permits PromotionFullReductionRes, PromotionOrderDiscountRes {}
