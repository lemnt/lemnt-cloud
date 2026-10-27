package com.lemnt.mall.application.checkout.executor;

import com.lemnt.mall.application.checkout.cmd.SettlementSubmitCmd;
import com.lemnt.mall.application.checkout.dto.CheckoutReceiptDTO;
import com.lemnt.mall.application.checkout.service.SettlementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** SettlementSubmitCmdExe，由应用服务保持结算事务和幂等语义。 */
@Component
@RequiredArgsConstructor
public class SettlementSubmitCmdExe {
    private final SettlementService settlementService;

    /** 执行当前结算操作。 */
    public CheckoutReceiptDTO execute(SettlementSubmitCmd cmd) {
        return settlementService.submit(cmd.memberId(), cmd.settlementId(), cmd.requestNo());
    }
}
