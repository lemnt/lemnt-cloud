package com.lemnt.promotion.application.coupon.qry;

import java.io.Serializable;

/**
 * 营销分页查询。
 */
public record CouponPageQry(Long memberId, int pageNum, int pageSize) implements Serializable {}
