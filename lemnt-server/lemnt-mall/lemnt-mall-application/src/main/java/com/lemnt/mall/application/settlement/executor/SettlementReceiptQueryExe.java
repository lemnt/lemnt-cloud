package com.lemnt.mall.application.settlement.executor;

import com.lemnt.mall.application.settlement.converter.SettlementDTOConverter;
import com.lemnt.mall.application.settlement.dto.SettlementReceiptDTO;
import com.lemnt.mall.application.settlement.service.SettlementRecordService;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.model.SettlementRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 结算回执查询执行器。
 */
@Component
@RequiredArgsConstructor
public class SettlementReceiptQueryExe {
    private final SettlementRecordService settlementRecordService;
    private final OrderGateway orderPersistenceService;

    /**
     * 执行结算回执查询。
     */
    public SettlementReceiptDTO execute(String orderNo) {
        String key = SettlementRules.text(orderNo, 40);
        return SettlementDTOConverter.receipt(settlementRecordService.receipt(key),
            orderPersistenceService.findByOrderNo(key));
    }

    /**
     * 执行结算回执查询。
     */
    public SettlementReceiptDTO execute(Long memberId, String orderNo) {
        SettlementRules.id(memberId);
        String key = SettlementRules.text(orderNo, 40);
        var order = orderPersistenceService.findByOrderNo(key);
        if (order == null || !memberId.equals(order.memberId())) {
            return null;
        }
        var record = settlementRecordService.receipt(
                key
        );
        if (record == null || !memberId.equals(record.memberId())) {
            return null;
        }
        return SettlementDTOConverter.receipt(record, order);
    }
}
