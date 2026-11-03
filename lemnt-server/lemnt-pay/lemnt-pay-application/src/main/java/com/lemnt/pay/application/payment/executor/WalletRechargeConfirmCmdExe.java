package com.lemnt.pay.application.payment.executor;

import com.lemnt.pay.application.payment.converter.RechargeDTOConverter;
import com.lemnt.pay.application.payment.dto.WalletRechargeDTO;
import com.lemnt.pay.application.payment.service.RechargeService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 后台确认充值到账（暂无真实渠道，代替支付回调）。 */
@Component
@RequiredArgsConstructor
public class WalletRechargeConfirmCmdExe {
    private final RechargeService service;

    public WalletRechargeDTO execute(String rechargeNo, String channelCode) {
        return RechargeDTOConverter.recharge(service.confirmPaid(rechargeNo, channelCode));
    }
}
