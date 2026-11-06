package com.lemnt.promotion.application.coupon.executor;

import com.lemnt.promotion.application.coupon.service.CouponService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 营销查询执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponPublicTemplateListQueryExe {
    private final CouponService couponService;

    /**
     * 执行营销查询。
     */
    public List<Long> execute() {
        return couponService.publicIds();
    }
}
