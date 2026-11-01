package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 小程序满减送详情。 */
@Schema(description = "小程序满减送详情")
public record AppRewardActivityRes(
        @Schema(description = "活动编号") Long id,
        @Schema(description = "活动名称") String name,
        @Schema(description = "开始时间") LocalDateTime startTime,
        @Schema(description = "结束时间") LocalDateTime endTime,
        @Schema(description = "条件类型 PRICE/COUNT") String conditionType,
        @Schema(description = "商品范围") String productScope,
        @Schema(description = "规则") List<Rule> rules
) implements java.io.Serializable {
    public AppRewardActivityRes {
        rules = rules == null ? List.of() : List.copyOf(rules);
    }

    /** 满减规则。 */
    @Schema(description = "满减规则")
    public record Rule(
            @Schema(description = "门槛") BigDecimal limit,
            @Schema(description = "优惠金额，元") BigDecimal discountPrice,
            @Schema(description = "是否包邮") Boolean freeDelivery,
            @Schema(description = "赠送积分") Integer point,
            @Schema(description = "赠送优惠券") Map<Long, Integer> giveCouponTemplateCounts
    ) implements java.io.Serializable {
    }
}
