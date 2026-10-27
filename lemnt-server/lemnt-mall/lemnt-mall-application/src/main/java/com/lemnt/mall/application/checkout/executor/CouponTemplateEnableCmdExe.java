package com.lemnt.mall.application.checkout.executor;

import com.lemnt.mall.application.checkout.cmd.CouponTemplateEnableCmd;
import com.lemnt.mall.domain.checkout.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.checkout.model.CheckoutRules;

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
        CheckoutRules.id(cmd.id());
        return couponTemplateGateway.enableTemplate(cmd.id(), cmd.enabled());
    }
}
