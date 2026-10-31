package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.converter.RechargeDTOConverter;
import com.lemnt.trade.application.payment.dto.WalletRechargeDTO;
import com.lemnt.trade.domain.payment.gateway.WalletRechargeGateway;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 充值单详情。 */
@Component
@RequiredArgsConstructor
public class WalletRechargeDetailQueryExe {
    private final WalletRechargeGateway recharges;

    public WalletRechargeDTO execute(String rechargeNo) {
        return RechargeDTOConverter.recharge(recharges.findByNo(OrderRules.text(rechargeNo, 40)));
    }
}
