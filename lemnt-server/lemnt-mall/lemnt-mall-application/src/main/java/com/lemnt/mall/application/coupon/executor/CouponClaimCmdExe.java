package com.lemnt.mall.application.coupon.executor;

import com.lemnt.mall.application.coupon.cmd.CouponClaimCmd;
import com.lemnt.mall.application.coupon.dto.CouponDTO;
import com.lemnt.mall.application.coupon.service.CouponService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 商城命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponClaimCmdExe {
    private final CouponService couponService;

    /**
     * 执行优惠券领取命令。
     */
    public CouponDTO execute(CouponClaimCmd c) {
        return couponService.claim(c);
    }
}
