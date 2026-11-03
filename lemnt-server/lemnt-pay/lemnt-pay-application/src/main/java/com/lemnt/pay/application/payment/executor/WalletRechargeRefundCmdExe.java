package com.lemnt.pay.application.payment.executor;

import com.lemnt.pay.application.payment.converter.RechargeDTOConverter;
import com.lemnt.pay.application.payment.dto.WalletRechargeDTO;
import com.lemnt.pay.application.payment.service.RechargeService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 后台充值退款，余额不足时拒绝。 */
@Component
@RequiredArgsConstructor
public class WalletRechargeRefundCmdExe {
    private final RechargeService service;

    public WalletRechargeDTO execute(String rechargeNo) {
        return RechargeDTOConverter.recharge(service.refund(rechargeNo));
    }
}
