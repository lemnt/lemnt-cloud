package com.lemnt.mall.adapter.app.store.converter;

import com.lemnt.mall.adapter.app.store.req.CartAddReq;
import com.lemnt.mall.adapter.app.store.req.MallAppCouponCenterOffersReq;
import com.lemnt.mall.adapter.app.store.req.MallAppCouponCenterWalletReq;
import com.lemnt.mall.adapter.app.store.req.PromotionPageReq;
import com.lemnt.mall.application.store.cmd.CartAddCmd;
import com.lemnt.mall.application.store.qry.CouponOffersPageQry;
import com.lemnt.mall.application.store.qry.CouponWalletPageQry;
import com.lemnt.mall.application.store.qry.PromotionPageQry;

/**
 * 营销请求转换器。
 */
public final class PromotionReqConverter {
    private PromotionReqConverter() {}

    /**
     * 转换分页查询或分页响应。
     */
    public static PromotionPageQry page(Long m, PromotionPageReq r) {
        return new PromotionPageQry(
                m, r.pageNum() == null ? 1 : r.pageNum(), r.pageSize() == null ? 20 : r.pageSize());
    }

    /**
     * 新增。
     */
    public static CartAddCmd add(Long m, Long p, CartAddReq r) {
        return new CartAddCmd(m, p, r.quantity(), r.requestNo());
    }

    /**
     * 可领取优惠。
     */
    public static CouponOffersPageQry offers(Long m, PromotionPageReq r) {
        var q = page(m, r);
        return new CouponOffersPageQry(m, q.pageNum(), q.pageSize());
    }

    /**
     * 券包。
     */
    public static CouponWalletPageQry wallet(Long m, PromotionPageReq r) {
        var q = page(m, r);
        return new CouponWalletPageQry(m, q.pageNum(), q.pageSize());
    }

    /**
     * 可领取优惠。
     */
    public static CouponOffersPageQry offers(Long m, MallAppCouponCenterOffersReq r) {
        var q = page(m, r);
        return new CouponOffersPageQry(m, q.pageNum(), q.pageSize());
    }

    /**
     * 券包。
     */
    public static CouponWalletPageQry wallet(Long m, MallAppCouponCenterWalletReq r) {
        var q = page(m, r);
        return new CouponWalletPageQry(m, q.pageNum(), q.pageSize());
    }

    /** 转换当前接口的分页参数。 */
    public static PromotionPageQry page(Long memberId, MallAppCouponCenterOffersReq req) {
        return new PromotionPageQry(memberId,
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }

    /** 转换当前接口的分页参数。 */
    public static PromotionPageQry page(Long memberId, MallAppCouponCenterWalletReq req) {
        return new PromotionPageQry(memberId,
                req.pageNum() == null ? 1 : req.pageNum(),
                req.pageSize() == null ? 20 : req.pageSize());
    }
}
