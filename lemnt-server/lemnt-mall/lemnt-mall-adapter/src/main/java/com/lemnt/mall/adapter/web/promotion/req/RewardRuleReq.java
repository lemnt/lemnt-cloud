package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** 满减规则请求。 */
@Schema(description = "满减规则请求")
public record RewardRuleReq(
        @Schema(description = "门槛，金额单位元或购买件数")
        @NotNull @DecimalMin("0.01")
        BigDecimal limit,
        @Schema(description = "优惠金额，单位元")
        @NotNull @DecimalMin("0.01")
        BigDecimal discountPrice
) implements java.io.Serializable {
}
