package com.lemnt.mall.application.coupon.executor;

import com.lemnt.mall.domain.coupon.gateway.CouponGateway;
import com.lemnt.mall.domain.coupon.gateway.CouponTemplateGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 到期任务仅更新未使用且没有订单锁定的券。 */
@Component
@RequiredArgsConstructor
public class CouponExpireCmdExe {
    private final CouponGateway coupons;
    private final CouponTemplateGateway templates;

    /** 同一数据库时钟下执行过期，重复执行不改变其他状态。 */
    @Transactional(rollbackFor = Exception.class)
    public int execute() {
        return coupons.expire(templates.now());
    }
}
