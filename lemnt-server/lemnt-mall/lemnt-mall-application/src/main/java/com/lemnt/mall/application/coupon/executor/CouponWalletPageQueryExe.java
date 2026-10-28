package com.lemnt.mall.application.coupon.executor;

import com.lemnt.mall.application.coupon.dto.CouponWalletPageDTO;
import com.lemnt.mall.application.coupon.qry.CouponPageQry;
import com.lemnt.mall.application.coupon.qry.CouponWalletPageQry;
import com.lemnt.mall.application.coupon.service.CouponService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 营销查询执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponWalletPageQueryExe {
    private final CouponService couponService;

    /**
     * 执行优惠券券包分页查询。
     */
    public CouponWalletPageDTO execute(CouponWalletPageQry q) {
        return couponService.wallet(new CouponPageQry(q.memberId(), q.pageNum(), q.pageSize()));
    }
}
