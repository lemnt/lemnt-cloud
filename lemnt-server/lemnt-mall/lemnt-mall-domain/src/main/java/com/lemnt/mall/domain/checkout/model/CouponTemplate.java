package com.lemnt.mall.domain.checkout.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        boolean enabled
) {}
