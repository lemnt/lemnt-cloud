package com.lemnt.mall.domain.coupon.model;

/** 后台持券分页条件。 */
public record CouponFilter(Long memberId, Long templateId, String status, int pageNum, int pageSize) {
}
