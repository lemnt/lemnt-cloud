package com.lemnt.promotion.domain.promotion.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/** 满金额或满件数时的减免、包邮、赠积分和赠券。 */
public record RewardRule(
        BigDecimal limit,
        BigDecimal discountPrice,
        Boolean freeDelivery,
        Integer point,
        Map<Long, Integer> giveCouponTemplateCounts)
        implements Serializable {
    public RewardRule {
        giveCouponTemplateCounts =
                giveCouponTemplateCounts == null ? Map.of() : Map.copyOf(giveCouponTemplateCounts);
    }

    /** 兼容仅配置金额减免的历史规则。 */
    public RewardRule(BigDecimal limit, BigDecimal discountPrice) {
        this(limit, discountPrice, false, 0, Map.of());
    }
}
