package com.lemnt.mall.application.coupon.cmd;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券模板创建命令。
 */
public record CouponTemplateCreateCmd(
        String code,
        String name,
        String kind,
        BigDecimal threshold,
        BigDecimal fixedAmount,
        Integer discountPercent,
        BigDecimal discountLimit,
        int totalCount,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        String description,
        String takeType,
        int takeLimitCount,
        String productScope,
        List<Long> productScopeValues,
        String validityType,
        Integer fixedStartTerm,
        Integer fixedEndTerm
) implements Serializable {
    /** 存量调用按原固定日期、单人一张规则兼容读取。 */
    public CouponTemplateCreateCmd(
            String code,
            String name,
            String kind,
            BigDecimal threshold,
            BigDecimal fixedAmount,
            Integer discountPercent,
            BigDecimal discountLimit,
            int totalCount,
            LocalDateTime validFrom,
            LocalDateTime validUntil) {
        this(
                code,
                name,
                kind,
                threshold,
                fixedAmount,
                discountPercent,
                discountLimit,
                totalCount,
                validFrom,
                validUntil,
                null,
                "ADMIN",
                1,
                "ALL",
                List.of(),
                "DATE",
                null,
                null);
    }
}
