package com.lemnt.pay.application.payment.executor;

import com.lemnt.pay.application.payment.converter.RechargeDTOConverter;
import com.lemnt.pay.application.payment.dto.WalletRechargeDTO;
import com.lemnt.pay.domain.payment.gateway.WalletRechargeGateway;
import com.lemnt.pay.domain.payment.model.PaymentRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 充值单详情。
 */
@Component
@RequiredArgsConstructor
public class WalletRechargeDetailQueryExe {
    private final WalletRechargeGateway recharges;

    public WalletRechargeDTO execute(String rechargeNo) {
        return RechargeDTOConverter.recharge(recharges.findByNo(PaymentRules.text(rechargeNo, 40)));
    }
}
