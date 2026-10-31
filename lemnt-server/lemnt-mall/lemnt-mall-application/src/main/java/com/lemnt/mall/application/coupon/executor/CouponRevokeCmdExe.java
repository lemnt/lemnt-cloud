package com.lemnt.mall.application.coupon.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.domain.coupon.gateway.CouponGateway;
import com.lemnt.mall.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.coupon.model.CouponRules;
import com.lemnt.mall.domain.member.gateway.MemberLockGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 撤回未用券并归还模板名额。 */
@Component
@RequiredArgsConstructor
public class CouponRevokeCmdExe {
    private final MemberLockGateway members;
    private final CouponGateway coupons;
    private final CouponTemplateGateway templates;

    /** 撤回与发券使用相同会员锁，持券更新和计数在同一事务内。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(Long memberId, Long couponId) {
        CouponRules.id(memberId);
        CouponRules.id(couponId);
        members.lockMember(memberId);
        var coupon = coupons.coupon(memberId, couponId, true);
        if (coupon == null || !coupons.revoke(memberId, couponId)) {
            throw new ServiceException("优惠券不存在或已锁定、已使用，不能撤回", 400);
        }
        templates.changeCounts(coupon.templateId(), -1, 0);
        return true;
    }
}
