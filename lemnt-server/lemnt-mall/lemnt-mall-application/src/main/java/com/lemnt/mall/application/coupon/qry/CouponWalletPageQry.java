package com.lemnt.mall.application.coupon.qry;

import java.io.Serializable;

/**
 * 优惠券券包分页查询。
 */
public record CouponWalletPageQry(
        Long memberId,
        int pageNum,
        int pageSize
) implements Serializable {}
