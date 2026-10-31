package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.cmd.RechargePackageUpdateCmd;
import com.lemnt.trade.application.payment.support.RechargeRules;
import com.lemnt.trade.domain.payment.gateway.RechargePackageGateway;
import com.lemnt.trade.domain.payment.model.RechargePackage;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 修改充值套餐。 */
@Component
@RequiredArgsConstructor
public class RechargePackageUpdateCmdExe {
    private final RechargePackageGateway packages;

    @Transactional(rollbackFor = Exception.class)
    public boolean execute(RechargePackageUpdateCmd cmd) {
        if (cmd == null || cmd.id() == null || cmd.id() <= 0) {
            throw new IllegalArgumentException("套餐编号不合法");
        }
        var existing = packages.find(cmd.id());
        if (existing == null) {
            throw new IllegalArgumentException("充值套餐不存在");
        }
        String name = RechargeRules.name(cmd.name());
        RechargeRules.status(cmd.status());
        if (packages.existsName(name, cmd.id())) {
            throw new IllegalStateException("套餐名已存在");
        }
        var value = new RechargePackage(existing.id(), name,
                RechargeRules.positiveMoney(cmd.payPrice(), "支付金额"),
                RechargeRules.nonNegativeMoney(cmd.bonusPrice(), "赠送金额"), cmd.status(), existing.createTime());
        return packages.update(value);
    }
}
