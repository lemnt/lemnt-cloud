package com.lemnt.mall.application.settlement.service;

import com.lemnt.mall.application.settlement.converter.SettlementDTOConverter;
import com.lemnt.mall.application.settlement.dto.SettlementAbandonDTO;
import com.lemnt.mall.application.settlement.dto.SettlementDTO;
import com.lemnt.mall.application.settlement.dto.SettlementReceiptDTO;
import com.lemnt.mall.application.settlement.qry.SettlementQuoteQry;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.gateway.SettlementGateway;
import com.lemnt.mall.domain.settlement.model.SettlementRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 结算服务。
 */
@Component
@RequiredArgsConstructor
public class SettlementService {
    private final SettlementRecordService settlementRecordService;
    private final OrderGateway orderPersistenceService;
    private final OrderPlacementService orderPlacementService;
    private final SettlementGateway settlementGateway;

    /**
     * 创建。
     */
    public SettlementDTO create(SettlementQuoteQry query) {
        var value = orderPlacementService.prepareSettlement(query);
        settlementGateway.cache(value);
        return new SettlementDTO(
                value.id(), value.expiresAt(), SettlementDTOConverter.quote(value.quote()));
    }

    /**
     * 提交。
     */
    public SettlementReceiptDTO submit(Long memberId, String id, String requestNo) {
        var order = orderPlacementService.submitSettlement(memberId, id, requestNo, settlementGateway);
        return SettlementDTOConverter.receipt(settlementRecordService.receipt(order.orderNo()), order);
    }

    /**
     * 查找按请求。
     */
    public SettlementReceiptDTO findByRequest(Long memberId, String requestNo) {
        SettlementRules.id(memberId);
        String key = SettlementRules.text(requestNo, 64);
        var record = settlementRecordService.findRequest(
                memberId,
                key
        );
        if (record == null || !memberId.equals(record.memberId())) {
            return null;
        }
        var order = orderPersistenceService.findByOrderNo(record.orderNo());
        if (order == null || !memberId.equals(order.memberId())) {
            return null;
        }
        return SettlementDTOConverter.receipt(record, order);
    }

    /**
     * 放弃。
     */
    public SettlementAbandonDTO abandon(
            Long memberId,
            String id,
            String requestNo
    ) {
        var order = orderPlacementService.abandonSettlement(memberId, id, requestNo, settlementGateway);
        return new SettlementAbandonDTO(
                order == null,
                order == null
                ? null
                : SettlementDTOConverter.receipt(settlementRecordService.receipt(order.orderNo()), order));
    }
}
