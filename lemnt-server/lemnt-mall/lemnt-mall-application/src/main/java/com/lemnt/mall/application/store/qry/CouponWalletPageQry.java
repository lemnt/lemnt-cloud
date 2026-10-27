package com.lemnt.mall.application.store.qry;

/**
 * 优惠券券包分页查询。
 */
public record CouponWalletPageQry(
        Long memberId,
        int pageNum,
        int pageSize
) implements java.io.Serializable {}
