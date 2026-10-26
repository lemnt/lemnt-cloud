package com.lemnt.trade.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Schema(description = "PromotionRuleReq")
public record PromotionRuleReq(
        @Schema(description = "满减阶梯，仅FULL_REDUCTION") @Size(max = 20)
                java.util.List<@Valid PromotionTierReq> tiers,
        @Schema(description = "折扣门槛整数分") @Pattern(regexp = "^(0|[1-9][0-9]{0,18})$")
                String thresholdFen,
        @Schema(description = "支付比例基点，8000为80%") @Min(1) @Max(9999) Integer payRateBps,
        @Schema(description = "正整数分上限，null无限") @Pattern(regexp = "^[1-9][0-9]{0,18}$")
                String maxDiscountFen)
        implements java.io.Serializable {}
