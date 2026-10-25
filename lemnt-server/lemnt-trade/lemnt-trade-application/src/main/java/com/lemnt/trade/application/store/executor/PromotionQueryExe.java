package com.lemnt.trade.application.store.executor;

import com.lemnt.trade.application.store.qry.*;
import com.lemnt.trade.application.store.dto.PromotionPageDTO;
import com.lemnt.trade.application.store.service.StorefrontService;

@org.springframework.stereotype.Component
@lombok.RequiredArgsConstructor
public class PromotionQueryExe {
    private final StorefrontService service;

    public PromotionPageDTO execute(CouponOffersPageQry q) {
        return service.offers(new PromotionPageQry(q.memberId(), q.pageNum(), q.pageSize()));
    }

    public PromotionPageDTO execute(CouponWalletPageQry q) {
        return service.wallet(new PromotionPageQry(q.memberId(), q.pageNum(), q.pageSize()));
    }

    public java.util.List<Long> execute() {
        return service.publicIds();
    }
}
