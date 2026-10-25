package com.lemnt.trade.adapter.web.store.converter;

import com.lemnt.trade.adapter.web.store.req.*;
import com.lemnt.trade.application.store.cmd.*;
import com.lemnt.trade.application.store.qry.*;

public final class PromotionReqConverter {
    private PromotionReqConverter() {}

    public static PromotionPageQry page(Long m, PromotionPageReq r) {
        return new PromotionPageQry(
                m, r.pageNum() == null ? 1 : r.pageNum(), r.pageSize() == null ? 20 : r.pageSize());
    }

    public static CartAddCmd add(Long m, Long p, CartAddReq r) {
        return new CartAddCmd(m, p, r.quantity(), r.requestNo());
    }

    public static CouponPublicClaimCmd setting(Long t, CouponPublicClaimReq r) {
        return new CouponPublicClaimCmd(t, r.enabled());
    }

    public static CouponOffersPageQry offers(Long m, PromotionPageReq r) {
        var q = page(m, r);
        return new CouponOffersPageQry(m, q.pageNum(), q.pageSize());
    }

    public static CouponWalletPageQry wallet(Long m, PromotionPageReq r) {
        var q = page(m, r);
        return new CouponWalletPageQry(m, q.pageNum(), q.pageSize());
    }
}
