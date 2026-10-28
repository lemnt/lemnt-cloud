package com.lemnt.mall.application.coupon.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券模板数据传输对象。
 */
public record CouponTemplateDTO(
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
) implements Serializable {
    /** 存量调用按原固定日期、单人一张规则兼容读取。 */
    public CouponTemplateDTO(
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
