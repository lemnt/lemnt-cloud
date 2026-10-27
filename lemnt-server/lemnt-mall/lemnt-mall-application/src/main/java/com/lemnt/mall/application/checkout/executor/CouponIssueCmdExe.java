package com.lemnt.mall.application.checkout.executor;

import com.lemnt.mall.application.checkout.cmd.CouponIssueCmd;
import com.lemnt.mall.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.mall.application.checkout.dto.CouponDTO;
import com.lemnt.mall.application.checkout.service.CheckoutMemberService;
import com.lemnt.mall.application.checkout.service.CheckoutService;
import com.lemnt.mall.domain.checkout.model.CheckoutRules;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 优惠券签发命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponIssueCmdExe {
    private final CheckoutService checkoutService;
    private final CheckoutMemberService checkoutMemberService;

    /**
     * 执行优惠券签发命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public CouponDTO execute(CouponIssueCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("领券参数不能为空");
        }
        CheckoutRules.id(cmd.templateId());
        checkoutMemberService.requireActive(cmd.memberId());
        return CheckoutDTOConverter.coupon(checkoutService.issue(cmd.memberId(), cmd.templateId()));
    }
}
