package com.lemnt.promotion.adapter.app.coupon.converter;

import com.lemnt.promotion.adapter.app.coupon.req.CouponPageReq;
import com.lemnt.promotion.adapter.app.coupon.req.MallAppCouponCenterOffersReq;
import com.lemnt.promotion.adapter.app.coupon.req.MallAppCouponCenterWalletReq;
import com.lemnt.promotion.application.coupon.qry.CouponOffersPageQry;
import com.lemnt.promotion.application.coupon.qry.CouponPageQry;
import com.lemnt.promotion.application.coupon.qry.CouponWalletPageQry;

/**
 * 按业务域转换接口与应用数据。
 */
public final class CouponReqConverter {
    private CouponReqConverter() {
    }

    /**
     * 转换分页查询或分页响应。
     */
    public static CouponPageQry page(Long m, CouponPageReq r) {
        return new CouponPageQry(
            m, r.pageNum() == null ? 1 : r.pageNum(), r.pageSize() == null ? 20 : r.pageSize());
    }

    /**
     * 可领取优惠。
     */
    public static CouponOffersPageQry offers(Long m, CouponPageReq r) {
        var q = page(m, r);
        return new CouponOffersPageQry(m, q.pageNum(), q.pageSize());
    }

    /**
     * 券包。
     */
    public static CouponWalletPageQry wallet(Long m, CouponPageReq r) {
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

    /**
     * 转换当前接口的分页参数。
     */
    public static CouponPageQry page(Long memberId, MallAppCouponCenterOffersReq req) {
        return new CouponPageQry(
            memberId,
            req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize());
    }

    /**
     * 转换当前接口的分页参数。
     */
    public static CouponPageQry page(Long memberId, MallAppCouponCenterWalletReq req) {
        return new CouponPageQry(
            memberId,
            req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize());
    }
}
