package com.lemnt.mall.application.store.qry;

/**
 * 优惠券可领取优惠分页查询。
 */
public record CouponOffersPageQry(
        Long memberId,
        int pageNum,
        int pageSize
) implements java.io.Serializable {}
