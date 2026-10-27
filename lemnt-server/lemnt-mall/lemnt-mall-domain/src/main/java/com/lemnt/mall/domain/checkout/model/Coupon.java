package com.lemnt.mall.domain.checkout.model;

/**
 * 优惠券。
 */
public record Coupon(
        Long id,
        Long templateId,
        Long memberId,
        String status,
        String orderNo
) {}
