package com.lemnt.mall.application.coupon.executor;

import com.lemnt.mall.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.coupon.model.CouponRules;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 删除模板保留已发券权益。 */
@Component
@RequiredArgsConstructor
public class CouponTemplateDeleteCmdExe {
    private final CouponTemplateGateway templates;

    /** 通过模板行锁与并发发券串行化。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean execute(Long id) {
        CouponRules.id(id);
        return templates.lockTemplate(id) != null && templates.deleteTemplate(id);
    }
}
