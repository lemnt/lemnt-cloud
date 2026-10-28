package com.lemnt.mall.application.coupon.executor;

import com.lemnt.mall.application.coupon.cmd.CouponPublicClaimCmd;
import com.lemnt.mall.application.coupon.service.CouponService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 商城命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponPublicClaimCmdExe {
    private final CouponService couponService;

    /**
     * 执行优惠券公开领取命令。
     */
    public boolean execute(CouponPublicClaimCmd c) {
        return couponService.publicClaim(c);
    }
}
