package com.lemnt.mall.application.checkout.executor;

import com.lemnt.mall.application.checkout.cmd.SettlementAbandonCmd;
import com.lemnt.mall.application.checkout.dto.SettlementAbandonDTO;
import com.lemnt.mall.application.checkout.service.SettlementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** SettlementAbandonCmdExe，由应用服务保持结算事务和幂等语义。 */
@Component
@RequiredArgsConstructor
public class SettlementAbandonCmdExe {
    private final SettlementService settlementService;

    /** 执行当前结算操作。 */
    public SettlementAbandonDTO execute(SettlementAbandonCmd cmd) {
        return settlementService.abandon(cmd.memberId(), cmd.settlementId(), cmd.requestNo());
    }
}
