package com.lemnt.promotion.application.coupon.service;

import com.lemnt.promotion.domain.coupon.gateway.CouponGateway;
import com.lemnt.promotion.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.promotion.domain.coupon.model.CouponOrderReference;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 原订单券的核销和整单退回仅在营销库执行，与本库事件消费同事务。 */
@Component
@RequiredArgsConstructor
public class CouponLifecycleService {
    private final CouponGateway coupons;
    private final CouponTemplateGateway templates;

    @Transactional(rollbackFor = Exception.class)
    public void useCoupon(String orderNo) {
        var coupon = coupons.byOrderNo(orderNo);
        if (coupon != null
                && coupons.useCoupon(
                        new CouponOrderReference(coupon.memberId(), coupon.id(), orderNo))) {
            templates.changeCounts(coupon.templateId(), 0, 1);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void returnUsedCoupon(String orderNo) {
        var coupon = coupons.byOrderNo(orderNo);
        if (coupon != null && coupons.returnUsedCoupon(orderNo)) {
            templates.changeCounts(coupon.templateId(), 0, -1);
        }
    }
}
