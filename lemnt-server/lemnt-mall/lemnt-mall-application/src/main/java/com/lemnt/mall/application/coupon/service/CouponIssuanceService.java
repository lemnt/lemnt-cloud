package com.lemnt.mall.application.coupon.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.domain.coupon.gateway.CouponGateway;
import com.lemnt.mall.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.coupon.model.Coupon;
import com.lemnt.mall.domain.coupon.model.CouponRules;
import com.lemnt.mall.domain.member.gateway.MemberLockGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 优惠券签发编排。 */
@Component
@RequiredArgsConstructor
public class CouponIssuanceService {
    private final MemberLockGateway memberLockGateway;
    private final CouponTemplateGateway couponTemplateGateway;
    private final CouponGateway couponGateway;

    /** 后台发放使用独立领取类型，不占用会员主动领取入口。 */
    @Transactional(rollbackFor = Exception.class)
    public Coupon issue(Long memberId, Long templateId) {
        return take(memberId, templateId, "ADMIN");
    }

    /** 在会员锁和模板行锁内校验限领、占用名额并固化持券规则。 */
    @Transactional(rollbackFor = Exception.class)
    public Coupon take(Long memberId, Long templateId, String takeType) {
        memberLockGateway.lockMember(memberId);
        var template = couponTemplateGateway.lockTemplate(templateId);
        var now = couponTemplateGateway.now();
        if (template == null || !template.enabled() || !takeType.equals(template.takeType())
                || ("DATE".equals(template.validityType()) && !template.validUntil().isAfter(now))) {
            throw new ServiceException("优惠券当前不可领取", 400);
        }
        if (template.takeLimitCount() != -1
                && couponGateway.takeCount(memberId, templateId) >= template.takeLimitCount()) {
            throw new ServiceException("超过优惠券领取上限", 400);
        }
        if (!couponTemplateGateway.takeQuota(templateId)) {
            throw new ServiceException("优惠券名额已用完", 400);
        }
        return couponGateway.insertCoupon(new Coupon(null, templateId, memberId, "AVAILABLE", null,
                CouponRules.snapshot(template, now), null));
    }
}
