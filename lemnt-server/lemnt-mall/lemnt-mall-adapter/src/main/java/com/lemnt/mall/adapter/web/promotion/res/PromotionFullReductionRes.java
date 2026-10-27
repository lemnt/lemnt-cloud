package com.lemnt.mall.adapter.web.promotion.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 营销满减响应。
 */
@Schema(description = "PromotionFullReductionRes")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record PromotionFullReductionRes(
        @Schema(description = "最高达标档一次优惠")
        List<PromotionTierRes> tiers
) implements PromotionRuleRes {}
