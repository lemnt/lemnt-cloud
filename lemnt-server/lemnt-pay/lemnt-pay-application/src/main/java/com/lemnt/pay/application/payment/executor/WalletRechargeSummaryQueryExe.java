package com.lemnt.pay.application.payment.executor;

import com.lemnt.pay.domain.payment.gateway.WalletRechargeGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.RoundingMode;

/** 查询充值单实付毛额，不合计赠送金额，也不从分页结果推算。 */
@Component
@RequiredArgsConstructor
public class WalletRechargeSummaryQueryExe {
    private final WalletRechargeGateway gateway;

    /** 返回两位小数的人民币元文本。 */
    public String execute() {
        return gateway.paidRechargeTotal().setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }
}
