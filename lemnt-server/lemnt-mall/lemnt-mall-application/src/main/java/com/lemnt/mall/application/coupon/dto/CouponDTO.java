package com.lemnt.mall.application.coupon.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 优惠券数据传输对象。
 */
public record CouponDTO(
        Long id,
        Long templateId,
        Long memberId,
        String status,
        String orderNo,
        CouponTermsDTO terms,
        LocalDateTime useTime
) implements Serializable {
    /** 存量调用按原固定日期、单人一张规则兼容读取。 */
    public CouponDTO(
            Long id,
            Long templateId,
            Long memberId,
            String status,
            String orderNo) {
        this(id, templateId, memberId, status, orderNo, null, null);
    }
}
