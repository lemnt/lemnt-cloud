package com.lemnt.mall.application.coupon.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.coupon.cmd.CouponTemplateUpdateCmd;
import com.lemnt.mall.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.coupon.model.CouponRules;
import com.lemnt.mall.domain.coupon.model.CouponTemplate;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 修改模板仅对后续发券生效。 */
@Component
@RequiredArgsConstructor
public class CouponTemplateUpdateCmdExe {
    private final CouponTemplateGateway templates;

    /** 校验规则与已发数量，计数及启停状态保持原值。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(CouponTemplateUpdateCmd cmd) {
        CouponRules.id(cmd.id());
        var current = templates.lockTemplate(cmd.id());
        if (current == null) {
            throw new ServiceException("优惠券模板不存在", 400);
        }
        var rule = CouponRules.template(new CouponTemplate(null, cmd.code(), cmd.name(), cmd.kind(),
                cmd.threshold(), cmd.fixedAmount(), cmd.discountPercent(), cmd.discountLimit(), cmd.totalCount(),
                0, cmd.validFrom(), cmd.validUntil(), true, cmd.description(), cmd.takeType(), cmd.takeLimitCount(),
                cmd.productScope(), cmd.productScopeValues(), cmd.validityType(), cmd.fixedStartTerm(), cmd.fixedEndTerm(), 0));
        if ("USER".equals(rule.takeType()) && rule.totalCount() != -1 && rule.totalCount() < current.issuedCount()) {
            throw new ServiceException("发放总量不能小于已发数量", 400);
        }
        return templates.updateTemplate(new CouponTemplate(current.id(), rule.code(), rule.name(), rule.kind(),
                rule.threshold(), rule.fixedAmount(), rule.discountPercent(), rule.discountLimit(), rule.totalCount(),
                current.issuedCount(), rule.validFrom(), rule.validUntil(), current.enabled(), rule.description(), rule.takeType(),
                rule.takeLimitCount(), rule.productScope(), rule.productScopeValues(), rule.validityType(), rule.fixedStartTerm(),
                rule.fixedEndTerm(), current.useCount()));
    }
}
