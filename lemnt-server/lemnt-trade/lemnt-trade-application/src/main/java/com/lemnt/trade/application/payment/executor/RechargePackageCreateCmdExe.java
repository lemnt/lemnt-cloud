package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.cmd.RechargePackageCreateCmd;
import com.lemnt.trade.application.payment.converter.RechargeDTOConverter;
import com.lemnt.trade.application.payment.dto.RechargePackageDTO;
import com.lemnt.trade.application.payment.support.RechargeRules;
import com.lemnt.trade.domain.payment.gateway.RechargePackageGateway;
import com.lemnt.trade.domain.payment.model.RechargePackage;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 新增充值套餐。 */
@Component
@RequiredArgsConstructor
public class RechargePackageCreateCmdExe {
    private final RechargePackageGateway packages;

    @Transactional(rollbackFor = Exception.class)
    public RechargePackageDTO execute(RechargePackageCreateCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("充值套餐参数不合法");
        }
        String name = RechargeRules.name(cmd.name());
        RechargeRules.status(cmd.status());
        if (packages.existsName(name, null)) {
            throw new IllegalStateException("套餐名已存在");
        }
        var value = new RechargePackage(null, name,
                RechargeRules.positiveMoney(cmd.payPrice(), "支付金额"),
                RechargeRules.nonNegativeMoney(cmd.bonusPrice(), "赠送金额"), cmd.status(), null);
        return RechargeDTOConverter.packageView(packages.insert(value));
    }
}
