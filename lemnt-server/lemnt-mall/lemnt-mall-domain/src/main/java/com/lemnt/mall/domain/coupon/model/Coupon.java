package com.lemnt.mall.domain.coupon.model;

import java.time.LocalDateTime;

/**
 * 优惠券。
 */
public record Coupon(
        Long id,
        Long templateId,
        Long memberId,
        String status,
        String orderNo,
        CouponTerms terms,
        LocalDateTime useTime
) {
    /** 存量调用按原固定日期、单人一张规则兼容读取。 */
    public Coupon(
            Long id,
            Long templateId,
            Long memberId,
            String status,
            String orderNo) {
        this(id, templateId, memberId, status, orderNo, null, null);
    }
}
