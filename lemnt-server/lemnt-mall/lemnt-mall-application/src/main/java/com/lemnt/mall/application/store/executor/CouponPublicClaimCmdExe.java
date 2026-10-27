package com.lemnt.mall.application.store.executor;

import com.lemnt.mall.application.store.cmd.CouponPublicClaimCmd;
import com.lemnt.mall.application.store.service.StorefrontService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 商城命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponPublicClaimCmdExe {
    private final StorefrontService storefrontService;

    /**
     * 执行优惠券公开领取命令。
     */
    public boolean execute(CouponPublicClaimCmd c) {
        return storefrontService.publicClaim(c);
    }
}
