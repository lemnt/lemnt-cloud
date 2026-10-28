package com.lemnt.mall.application.coupon.cmd;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 修改优惠券模板，不改写持券规则。 */
public record CouponTemplateUpdateCmd(
        Long id,
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
}
