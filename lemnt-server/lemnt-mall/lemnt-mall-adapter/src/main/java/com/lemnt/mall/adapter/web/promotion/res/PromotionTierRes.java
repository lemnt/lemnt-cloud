package com.lemnt.mall.adapter.web.promotion.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 营销阶梯响应。
 */
@Schema(description = "PromotionTierRes")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record PromotionTierRes(
        @Schema(description = "整数分门槛")
        String thresholdFen,
        @Schema(description = "整数分优惠")
        String discountFen
) implements Serializable {}
