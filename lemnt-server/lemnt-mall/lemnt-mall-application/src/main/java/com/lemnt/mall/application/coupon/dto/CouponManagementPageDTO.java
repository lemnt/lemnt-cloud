package com.lemnt.mall.application.coupon.dto;

import java.io.Serializable;
import java.util.List;

/** 后台持券分页结果。 */
public record CouponManagementPageDTO(List<CouponDTO> records, long total) implements Serializable {
}
