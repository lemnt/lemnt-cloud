package com.lemnt.trade.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "PromotionOrderDiscountRes")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record PromotionOrderDiscountRes(
        @Schema(description = "整数分门槛") String thresholdFen,
        @Schema(description = "支付比例基点") int payRateBps,
        @Schema(description = "整数分优惠上限，可空") String maxDiscountFen)
        implements PromotionRuleRes {}
