package com.lemnt.mall.application.store.executor;

import com.lemnt.mall.application.store.dto.PromotionPageDTO;
import com.lemnt.mall.application.store.qry.CouponOffersPageQry;
import com.lemnt.mall.application.store.qry.PromotionPageQry;
import com.lemnt.mall.application.store.service.StorefrontService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 营销查询执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponOffersPageQueryExe {
    private final StorefrontService storefrontService;

    /**
     * 执行优惠券可领取优惠分页查询。
     */
    public PromotionPageDTO execute(CouponOffersPageQry q) {
        return storefrontService.offers(new PromotionPageQry(q.memberId(), q.pageNum(), q.pageSize()));
    }
}
