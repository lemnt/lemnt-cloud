package com.lemnt.mall.domain.coupon.model;

/** 优惠券核销和释放所需的订单归属。 */
public record CouponOrderReference(Long memberId, Long couponId, String orderNo) {
}
