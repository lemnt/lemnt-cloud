package com.lemnt.trade.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "PromotionTierRes")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record PromotionTierRes(
        @Schema(description = "整数分门槛") String thresholdFen,
        @Schema(description = "整数分优惠") String discountFen)
        implements java.io.Serializable {}
