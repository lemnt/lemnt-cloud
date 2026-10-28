package com.lemnt.mall.application.coupon.converter;

import com.lemnt.mall.application.coupon.dto.CouponDTO;
import com.lemnt.mall.application.coupon.dto.CouponTemplateDTO;
import com.lemnt.mall.application.coupon.dto.CouponTermsDTO;
import com.lemnt.mall.domain.coupon.model.Coupon;
import com.lemnt.mall.domain.coupon.model.CouponTemplate;
import com.lemnt.mall.domain.coupon.model.CouponTerms;

/** 按业务域转换接口与应用数据。 */
public final class CouponDTOConverter {
    private CouponDTOConverter() {}

    /**
     * 优惠券。
     */
    public static CouponDTO coupon(Coupon value) {
        return new CouponDTO(
                value.id(), value.templateId(), value.memberId(), value.status(), value.orderNo(),
                terms(value.terms()), value.useTime());
    }

    /** 对外传输持券快照，不暴露领域对象。 */
    public static CouponTermsDTO terms(
            CouponTerms value) {
        return value == null ? null : new CouponTermsDTO(
                value.code(), value.name(), value.takeType(), value.kind(), value.threshold(),
                value.fixedAmount(), value.discountPercent(), value.discountLimit(), value.productScope(),
                value.productScopeValues(), value.validFrom(), value.validUntil());
    }

    /**
     * 模板。
     */
    public static CouponTemplateDTO template(CouponTemplate value) {
        return new CouponTemplateDTO(
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
}
