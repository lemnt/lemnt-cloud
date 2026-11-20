package com.lemnt.order.application.settlement.executor;

import com.lemnt.order.application.settlement.cmd.SettlementSubmitCmd;
import com.lemnt.order.application.settlement.dto.SettlementReceiptDTO;
import com.lemnt.order.application.settlement.service.SettlementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * SettlementSubmitCmdExe，由应用服务保持结算事务和幂等语义。
 */
@Component
@RequiredArgsConstructor
public class SettlementSubmitCmdExe {
    private final SettlementService settlementService;

    /**
     * 执行当前结算操作。
     */
    public SettlementReceiptDTO execute(SettlementSubmitCmd cmd) {
        return settlementService.submit(cmd.memberId(), cmd.settlementId(), cmd.requestNo());
    }
}
