package com.lemnt.mall.application.checkout.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        boolean enabled
) implements java.io.Serializable {}
