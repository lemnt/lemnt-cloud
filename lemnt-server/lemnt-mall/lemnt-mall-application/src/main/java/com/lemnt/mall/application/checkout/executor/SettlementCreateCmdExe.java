package com.lemnt.mall.application.checkout.executor;

import com.lemnt.mall.application.checkout.cmd.SettlementCreateCmd;
import com.lemnt.mall.application.checkout.dto.SettlementDTO;
import com.lemnt.mall.application.checkout.service.SettlementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** SettlementCreateCmdExe，由应用服务保持结算事务和幂等语义。 */
@Component
@RequiredArgsConstructor
public class SettlementCreateCmdExe {
    private final SettlementService settlementService;

    /** 执行当前结算操作。 */
    public SettlementDTO execute(SettlementCreateCmd cmd) {
        return settlementService.create(new com.lemnt.mall.application.checkout.qry.CheckoutQuoteQry(cmd.memberId(),
                cmd.productIds(),
                cmd.couponId()));
    }
}
