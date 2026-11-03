package com.lemnt.promotion.application.coupon.qry;

import java.io.Serializable;

/**
 * 优惠券可领取优惠分页查询。
 */
public record CouponOffersPageQry(Long memberId, int pageNum, int pageSize)
        implements Serializable {}
