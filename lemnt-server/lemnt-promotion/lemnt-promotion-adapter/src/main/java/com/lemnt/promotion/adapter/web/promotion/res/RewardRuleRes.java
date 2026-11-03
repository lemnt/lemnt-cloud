package com.lemnt.promotion.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** 满减规则响应。 */
@Schema(description = "满减规则响应")
public record RewardRuleRes(
        @Schema(description = "门槛，金额单位元或购买件数") BigDecimal limit,
        @Schema(description = "优惠金额，单位元") BigDecimal discountPrice,
        @Schema(description = "是否包邮") Boolean freeDelivery,
        @Schema(description = "赠送积分") Integer point,
        @Schema(description = "赠送优惠券") java.util.Map<Long, Integer> giveCouponTemplateCounts)
        implements java.io.Serializable {}
