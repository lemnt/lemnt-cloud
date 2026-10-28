package com.lemnt.mall.adapter.app.coupon.converter;

import com.lemnt.mall.adapter.app.coupon.res.CouponTemplateRes;
import com.lemnt.mall.adapter.app.coupon.res.MallAppCouponCenterClaimRes;
import com.lemnt.mall.adapter.app.coupon.res.MallAppCouponCenterOffersRes;
import com.lemnt.mall.adapter.app.coupon.res.MallAppCouponCenterWalletRes;
import com.lemnt.mall.adapter.app.coupon.res.MallAppCouponCouponsRes;
import com.lemnt.mall.application.coupon.dto.CouponDTO;
import com.lemnt.mall.application.coupon.dto.CouponOfferDTO;
import com.lemnt.mall.application.coupon.dto.CouponTemplateDTO;
import com.lemnt.mall.application.coupon.dto.CouponWalletDTO;

/** 按业务域转换接口与应用数据。 */
public final class CouponResConverter {
    private CouponResConverter() {}

    /**
     * 优惠券模板。
     */
    public static CouponTemplateRes couponTemplate(CouponTemplateDTO value) {
        return value == null
                ? null
                : new CouponTemplateRes(
                value.id(),
                value.code(),
                value.name(),
                value.kind(),
                value.threshold(),
                value.fixedAmount(),
                value.discountPercent(),
                value.discountLimit(),
                value.totalCount(),
                value.issuedCount(),
                value.validFrom(),
                value.validUntil(),
                value.enabled(),
                value.description(), value.takeType(), value.takeLimitCount(), value.productScope(), value.productScopeValues(), value.validityType(), value.fixedStartTerm(), value.fixedEndTerm(), value.useCount());
    }

    /**
     * 优惠券。
     */
    public static MallAppCouponCenterClaimRes couponToMallAppCouponCenterClaimRes(CouponDTO value) {
        return value == null
                ? null
                : new MallAppCouponCenterClaimRes(
                value.id(),
                value.templateId(),
                value.memberId(),
                value.status(),
                value.orderNo());
    }

    /**
     * 优惠券。
     */
    public static MallAppCouponCouponsRes couponToMallAppCouponCouponsRes(CouponDTO value) {
        return value == null
                ? null
                : new MallAppCouponCouponsRes(
                value.id(),
                value.templateId(),
                value.memberId(),
                value.status(),
                value.orderNo());
    }

    /** 转换当前接口的优惠券分页项。 */
    public static MallAppCouponCenterOffersRes toMallAppCouponCenterOffersRes(CouponOfferDTO value) {
        return new MallAppCouponCenterOffersRes(CouponResConverter.couponTemplate(value.template()),
                value.couponId(),
                value.couponStatus(),
                value.usable(),
                value.claimable());
    }

    /** 转换当前接口的优惠券分页项。 */
    public static MallAppCouponCenterWalletRes toMallAppCouponCenterWalletRes(
            CouponWalletDTO value) {
        var terms = value.terms();
        return new MallAppCouponCenterWalletRes(value.couponId(), value.templateId(), value.couponStatus(),
                new MallAppCouponCenterWalletRes.RuleRes(terms.code(), terms.name(), terms.takeType(), terms.kind(),
                        terms.threshold(), terms.fixedAmount(), terms.discountPercent(), terms.discountLimit(),
                        terms.productScope(), terms.productScopeValues(), terms.validFrom(), terms.validUntil()), value.usable());
    }
}
