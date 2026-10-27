package com.lemnt.mall.application.checkout.cmd;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        LocalDateTime validUntil
) implements java.io.Serializable {}
