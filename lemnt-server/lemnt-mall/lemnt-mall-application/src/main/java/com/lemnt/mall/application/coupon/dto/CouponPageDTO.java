package com.lemnt.mall.application.coupon.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 营销分页数据传输对象。
 */
public record CouponPageDTO(
        List<CouponOfferDTO> records,
        long total
) implements Serializable {}
