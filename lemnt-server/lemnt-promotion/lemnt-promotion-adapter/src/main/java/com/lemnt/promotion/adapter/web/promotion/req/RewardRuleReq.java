package com.lemnt.promotion.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/** 满减规则请求。 */
@Schema(description = "满减规则请求")
public record RewardRuleReq(
        @Schema(description = "门槛，金额单位元或购买件数") @NotNull @DecimalMin("0.01") BigDecimal limit,
        @Schema(description = "优惠金额，单位元") @NotNull @DecimalMin("0.01") BigDecimal discountPrice,
        @Schema(description = "是否包邮") Boolean freeDelivery,
        @Schema(description = "赠送积分") @Min(0) Integer point,
        @Schema(description = "赠送优惠券，key 为模板编号，value 为数量")
                Map<Long, Integer> giveCouponTemplateCounts)
        implements Serializable {}
