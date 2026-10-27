package com.lemnt.mall.application.checkout.executor;

import com.lemnt.mall.application.checkout.cmd.CouponTemplateCreateCmd;
import com.lemnt.mall.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.mall.application.checkout.dto.CouponTemplateDTO;
import com.lemnt.mall.domain.checkout.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.checkout.model.CheckoutRules;
import com.lemnt.mall.domain.checkout.model.CouponTemplate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 优惠券模板创建命令执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponTemplateCreateCmdExe {
    private final CouponTemplateGateway couponTemplateGateway;

    /**
     * 执行优惠券模板创建命令。
     */
    @Transactional(rollbackFor = Exception.class)
    public CouponTemplateDTO execute(CouponTemplateCreateCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("优惠券参数不能为空");
        }
        var value =
                CheckoutRules.template(
                new CouponTemplate(
                null,
                cmd.code(),
                cmd.name(),
                cmd.kind(),
                cmd.threshold(),
                cmd.fixedAmount(),
                cmd.discountPercent(),
                cmd.discountLimit(),
                cmd.totalCount(),
                0,
                cmd.validFrom(),
                cmd.validUntil(),
                true));
        return CheckoutDTOConverter.template(couponTemplateGateway.createTemplate(value));
    }
}
