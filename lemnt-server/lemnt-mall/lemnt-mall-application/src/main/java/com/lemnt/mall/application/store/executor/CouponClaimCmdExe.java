package com.lemnt.mall.application.store.executor;

import com.lemnt.mall.application.checkout.dto.CouponDTO;
import com.lemnt.mall.application.store.cmd.CouponClaimCmd;
import com.lemnt.mall.application.store.service.StorefrontService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 商城命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponClaimCmdExe {
    private final StorefrontService storefrontService;

    /**
     * 执行优惠券领取命令。
     */
    public CouponDTO execute(CouponClaimCmd c) {
        return storefrontService.claim(c);
    }
}
