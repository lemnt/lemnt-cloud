package com.lemnt.trade.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "PromotionTierReq")
public record PromotionTierReq(
        @Schema(description = "非负整数分门槛") @NotNull @Pattern(regexp = "^(0|[1-9][0-9]{0,18})$")
                String thresholdFen,
        @Schema(description = "正整数分优惠") @NotNull @Pattern(regexp = "^[1-9][0-9]{0,18}$")
                String discountFen)
        implements java.io.Serializable {}
