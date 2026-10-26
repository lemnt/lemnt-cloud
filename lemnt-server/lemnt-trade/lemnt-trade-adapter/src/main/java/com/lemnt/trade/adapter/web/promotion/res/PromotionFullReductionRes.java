package com.lemnt.trade.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "PromotionFullReductionRes")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record PromotionFullReductionRes(
        @Schema(description = "最高达标档一次优惠") java.util.List<PromotionTierRes> tiers)
        implements PromotionRuleRes {}
