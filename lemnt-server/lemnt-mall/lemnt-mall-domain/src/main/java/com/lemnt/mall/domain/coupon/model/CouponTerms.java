package com.lemnt.mall.domain.coupon.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 发放时冻结的持券规则，模板后续变化不得改写。 */
public record CouponTerms(
        String code,
        String name,
        String takeType,
        String kind,
        BigDecimal threshold,
        BigDecimal fixedAmount,
        Integer discountPercent,
        BigDecimal discountLimit,
        String productScope,
        List<Long> productScopeValues,
        LocalDateTime validFrom,
        LocalDateTime validUntil
) {
    public CouponTerms {
        productScopeValues = productScopeValues == null ? List.of() : List.copyOf(productScopeValues);
    }
}
