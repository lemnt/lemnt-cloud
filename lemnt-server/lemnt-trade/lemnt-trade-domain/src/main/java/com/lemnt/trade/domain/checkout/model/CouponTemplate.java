package com.lemnt.trade.domain.checkout.model;

public record CouponTemplate(
        Long id,
        String code,
        String name,
        String kind,
        java.math.BigDecimal threshold,
        java.math.BigDecimal fixedAmount,
        Integer discountPercent,
        java.math.BigDecimal discountLimit,
        int totalCount,
        int issuedCount,
        java.time.LocalDateTime validFrom,
        java.time.LocalDateTime validUntil,
        boolean enabled) {}
