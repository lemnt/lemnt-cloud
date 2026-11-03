package com.lemnt.order.application.settlement.executor;

import com.lemnt.order.application.settlement.cmd.SettlementCreateCmd;
import com.lemnt.order.application.settlement.dto.SettlementDTO;
import com.lemnt.order.application.settlement.qry.SettlementQuoteQry;
import com.lemnt.order.application.settlement.service.SettlementService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** SettlementCreateCmdExe，由应用服务保持结算事务和幂等语义。 */
@Component
@RequiredArgsConstructor
public class SettlementCreateCmdExe {
    private final SettlementService settlementService;

    /** 执行当前结算操作。 */
    public SettlementDTO execute(SettlementCreateCmd cmd) {
        return settlementService.create(
                new SettlementQuoteQry(
                        cmd.memberId(),
                        cmd.productIds(),
                        cmd.couponId(),
                        cmd.pointStatus(),
                        cmd.addressId(),
                        cmd.deliveryType(),
                        cmd.pickUpStoreId()));
    }
}
