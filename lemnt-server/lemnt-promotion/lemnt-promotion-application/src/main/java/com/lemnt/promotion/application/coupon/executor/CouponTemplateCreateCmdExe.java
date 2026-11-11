package com.lemnt.promotion.application.coupon.executor;

import com.lemnt.promotion.application.coupon.cmd.CouponTemplateCreateCmd;
import com.lemnt.promotion.application.coupon.converter.CouponDTOConverter;
import com.lemnt.promotion.application.coupon.dto.CouponTemplateDTO;
import com.lemnt.promotion.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.promotion.domain.coupon.model.CouponRules;
import com.lemnt.promotion.domain.coupon.model.CouponTemplate;

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
            CouponRules.template(
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
                    true,
                    cmd.description(),
                    cmd.takeType(),
                    cmd.takeLimitCount(),
                    cmd.productScope(),
                    cmd.productScopeValues(),
                    cmd.validityType(),
                    cmd.fixedStartTerm(),
                    cmd.fixedEndTerm(),
                    0));
        return CouponDTOConverter.template(couponTemplateGateway.createTemplate(value));
    }
}
