package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.domain.payment.gateway.RechargePackageGateway;
import com.lemnt.trade.domain.payment.gateway.WalletRechargeGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 删除充值套餐，已被充值单引用时拒绝。 */
@Component
@RequiredArgsConstructor
public class RechargePackageDeleteCmdExe {
    private final RechargePackageGateway packages;
    private final WalletRechargeGateway recharges;

    @Transactional(rollbackFor = Exception.class)
    public boolean execute(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("套餐编号不合法");
        }
        if (packages.find(id) == null) {
            throw new IllegalArgumentException("充值套餐不存在");
        }
        if (recharges.packageReferenced(id)) {
            throw new IllegalStateException("套餐已被充值单引用，不能删除");
        }
        return packages.delete(id);
    }
}
