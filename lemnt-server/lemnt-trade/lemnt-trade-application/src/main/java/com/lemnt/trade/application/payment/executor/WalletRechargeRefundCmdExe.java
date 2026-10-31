package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.converter.RechargeDTOConverter;
import com.lemnt.trade.application.payment.dto.WalletRechargeDTO;
import com.lemnt.trade.application.payment.service.RechargeService;

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
