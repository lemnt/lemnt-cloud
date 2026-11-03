package com.lemnt.promotion.application.coupon.executor;

import com.lemnt.promotion.application.coupon.cmd.CouponIssueCmd;
import com.lemnt.promotion.application.coupon.converter.CouponDTOConverter;
import com.lemnt.promotion.application.coupon.dto.CouponDTO;
import com.lemnt.promotion.application.coupon.service.CouponIssuanceService;
import com.lemnt.promotion.application.member.service.MemberValidationService;
import com.lemnt.promotion.domain.coupon.model.CouponRules;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 优惠券签发命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponIssueCmdExe {
    private final CouponIssuanceService couponIssuanceService;
    private final MemberValidationService memberValidationService;

    /**
     * 执行优惠券签发命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public CouponDTO execute(CouponIssueCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("领券参数不能为空");
        }
        CouponRules.id(cmd.templateId());
        memberValidationService.requireActive(cmd.memberId());
        return CouponDTOConverter.coupon(
                couponIssuanceService.issue(cmd.memberId(), cmd.templateId()));
    }
}
