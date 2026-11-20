package com.lemnt.promotion.application.coupon.executor;

import com.lemnt.promotion.application.coupon.cmd.CouponTemplateEnableCmd;
import com.lemnt.promotion.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.promotion.domain.coupon.model.CouponRules;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 优惠券模板启用命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponTemplateEnableCmdExe {
    private final CouponTemplateGateway couponTemplateGateway;

    /**
     * 执行优惠券模板启用命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(CouponTemplateEnableCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("模板参数不能为空");
        }
        CouponRules.id(cmd.id());
        return couponTemplateGateway.enableTemplate(cmd.id(), cmd.enabled());
    }
}
