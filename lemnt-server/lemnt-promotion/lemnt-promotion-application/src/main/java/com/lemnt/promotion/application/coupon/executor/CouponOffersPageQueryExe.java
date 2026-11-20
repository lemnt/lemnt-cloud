package com.lemnt.promotion.application.coupon.executor;

import com.lemnt.promotion.application.coupon.dto.CouponPageDTO;
import com.lemnt.promotion.application.coupon.qry.CouponOffersPageQry;
import com.lemnt.promotion.application.coupon.qry.CouponPageQry;
import com.lemnt.promotion.application.coupon.service.CouponService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 营销查询执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponOffersPageQueryExe {
    private final CouponService couponService;

    /**
     * 执行优惠券可领取优惠分页查询。
     */
    public CouponPageDTO execute(CouponOffersPageQry q) {
        return couponService.offers(new CouponPageQry(q.memberId(), q.pageNum(), q.pageSize()));
    }
}
