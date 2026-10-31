package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.cmd.WalletRechargeCreateCmd;
import com.lemnt.trade.application.payment.converter.RechargeDTOConverter;
import com.lemnt.trade.application.payment.dto.WalletRechargeDTO;
import com.lemnt.trade.application.payment.service.RechargeService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 会员按套餐创建充值单。 */
@Component
@RequiredArgsConstructor
public class WalletRechargeCreateCmdExe {
    private final RechargeService service;

    public WalletRechargeDTO execute(Long memberId, WalletRechargeCreateCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("充值参数不合法");
        }
        return RechargeDTOConverter.recharge(service.create(memberId, cmd.packageId(), cmd.requestNo()));
    }
}
