package com.lemnt.mall.adapter.web.coupon.converter;

import com.lemnt.mall.adapter.web.coupon.res.CouponManagementPageRes;
import com.lemnt.mall.adapter.web.coupon.res.MallCouponCouponsRes;
import com.lemnt.mall.adapter.web.coupon.res.MallCouponIssueRes;
import com.lemnt.mall.adapter.web.coupon.res.MallCouponTemplateCreateRes;
import com.lemnt.mall.adapter.web.coupon.res.MallCouponTemplateTemplatesRes;
import com.lemnt.mall.application.coupon.dto.CouponDTO;
import com.lemnt.mall.application.coupon.dto.CouponTemplateDTO;

/** 按业务域转换接口与应用数据。 */
public final class CouponResConverter {
    private CouponResConverter() {}

    /**
     * 优惠券模板。
     */
    public static MallCouponTemplateTemplatesRes couponTemplateToMallCouponTemplateTemplatesRes(
            CouponTemplateDTO value) {
        return value == null
                ? null
                : new MallCouponTemplateTemplatesRes(
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
     * 优惠券模板。
     */
    public static MallCouponTemplateCreateRes couponTemplateToMallCouponTemplateCreateRes(CouponTemplateDTO value) {
        return value == null
                ? null
                : new MallCouponTemplateCreateRes(
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
    public static MallCouponIssueRes couponToMallCouponIssueRes(CouponDTO value) {
        return value == null
                ? null
                : new MallCouponIssueRes(
                value.id(),
                value.templateId(),
                value.memberId(),
                value.status(),
                value.orderNo());
    }

    /**
     * 优惠券。
     */
    public static MallCouponCouponsRes couponToMallCouponCouponsRes(CouponDTO value) {
        return value == null
                ? null
                : new MallCouponCouponsRes(
                value.id(),
                value.templateId(),
                value.memberId(),
                value.status(),
                value.orderNo());
    }
    /** 返回持券本身的规则，不依赖模板是否仍存在。 */
    public static CouponManagementPageRes page(CouponDTO value) {
        var terms = value.terms();
        var rule = terms == null ? null : new CouponManagementPageRes.RuleRes(
                terms.code(), terms.name(), terms.takeType(), terms.kind(), terms.threshold(), terms.fixedAmount(),
                terms.discountPercent(), terms.discountLimit(), terms.productScope(), terms.productScopeValues(),
                terms.validFrom(), terms.validUntil());
        return new CouponManagementPageRes(value.id(), value.templateId(), value.memberId(),
                value.status(), value.orderNo(), rule, value.useTime());
    }
}
