package com.lemnt.order.application.settlement.executor;

import com.lemnt.order.application.settlement.dto.SettlementReceiptDTO;
import com.lemnt.order.application.settlement.qry.SettlementRequestDetailQry;
import com.lemnt.order.application.settlement.service.SettlementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** SettlementRequestDetailQueryExe，由应用服务保持结算事务和幂等语义。 */
@Component
@RequiredArgsConstructor
public class SettlementRequestDetailQueryExe {
    private final SettlementService settlementService;

    /** 执行当前结算操作。 */
    public SettlementReceiptDTO execute(SettlementRequestDetailQry cmd) {
        return settlementService.findByRequest(cmd.memberId(), cmd.requestNo());
    }
}
