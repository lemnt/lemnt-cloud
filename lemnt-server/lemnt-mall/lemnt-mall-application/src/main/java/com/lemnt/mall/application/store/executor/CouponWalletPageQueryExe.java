package com.lemnt.mall.application.store.executor;

import com.lemnt.mall.application.store.dto.PromotionPageDTO;
import com.lemnt.mall.application.store.qry.CouponWalletPageQry;
import com.lemnt.mall.application.store.qry.PromotionPageQry;
import com.lemnt.mall.application.store.service.StorefrontService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 营销查询执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponWalletPageQueryExe {
    private final StorefrontService storefrontService;

    /**
     * 执行优惠券券包分页查询。
     */
    public PromotionPageDTO execute(CouponWalletPageQry q) {
        return storefrontService.wallet(new PromotionPageQry(q.memberId(), q.pageNum(), q.pageSize()));
    }
}
