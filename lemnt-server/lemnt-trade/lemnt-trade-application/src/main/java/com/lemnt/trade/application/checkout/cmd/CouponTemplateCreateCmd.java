package com.lemnt.trade.application.checkout.cmd;

public record CouponTemplateCreateCmd(
        String code,
        String name,
        String kind,
        java.math.BigDecimal threshold,
        java.math.BigDecimal fixedAmount,
        Integer discountPercent,
        java.math.BigDecimal discountLimit,
        int totalCount,
        java.time.LocalDateTime validFrom,
        java.time.LocalDateTime validUntil) {}
