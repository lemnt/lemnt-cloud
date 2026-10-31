package com.lemnt.mall.domain.coupon.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券模板。
 */
public record CouponTemplate(
        Long id,
        String code,
        String name,
        String kind,
        BigDecimal threshold,
        BigDecimal fixedAmount,
        Integer discountPercent,
        BigDecimal discountLimit,
        int totalCount,
        int issuedCount,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        boolean enabled,
        String description,
        String takeType,
        int takeLimitCount,
        String productScope,
        List<Long> productScopeValues,
        String validityType,
        Integer fixedStartTerm,
        Integer fixedEndTerm,
        int useCount
) {
    public CouponTemplate {
        productScopeValues = productScopeValues == null ? List.of() : List.copyOf(productScopeValues);
        takeType = "MANUAL".equals(takeType) ? "ADMIN" : takeType;
    }

    /** 存量调用按原固定日期、单人一张规则兼容读取。 */
    public CouponTemplate(
            Long id,
            String code,
            String name,
            String kind,
            BigDecimal threshold,
            BigDecimal fixedAmount,
            Integer discountPercent,
            BigDecimal discountLimit,
            int totalCount,
            int issuedCount,
            LocalDateTime validFrom,
            LocalDateTime validUntil,
            boolean enabled) {
        this(
                id,
                code,
                name,
                kind,
                threshold,
                fixedAmount,
                discountPercent,
                discountLimit,
                totalCount,
                issuedCount,
                validFrom,
                validUntil,
                enabled,
                null,
                "ADMIN",
                1,
                "ALL",
                List.of(),
                "DATE",
                null,
                null,
                0);
    }
}
