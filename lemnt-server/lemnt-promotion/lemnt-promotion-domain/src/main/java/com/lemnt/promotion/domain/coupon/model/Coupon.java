package com.lemnt.promotion.domain.coupon.model;

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
    LocalDateTime useTime,
    String sourceOrderNo,
    Integer giftSequence) {
    /**
     * 兼容普通持券，来源订单仅在营销赠券时填写。
     */
    public Coupon(
        Long id,
        Long templateId,
        Long memberId,
        String status,
        String orderNo,
        CouponTerms terms,
        LocalDateTime useTime) {
        this(id, templateId, memberId, status, orderNo, terms, useTime, null, null);
    }

    /**
     * 存量调用按原固定日期、单人一张规则兼容读取。
     */
    public Coupon(Long id, Long templateId, Long memberId, String status, String orderNo) {
        this(id, templateId, memberId, status, orderNo, null, null);
    }
}
