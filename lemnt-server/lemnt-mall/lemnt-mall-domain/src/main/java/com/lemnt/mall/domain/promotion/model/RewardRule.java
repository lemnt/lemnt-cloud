package com.lemnt.mall.domain.promotion.model;

import java.math.BigDecimal;

/** 满金额或满件数时减免金额。 */
public record RewardRule(
        BigDecimal limit,
        BigDecimal discountPrice
) implements java.io.Serializable {
}
