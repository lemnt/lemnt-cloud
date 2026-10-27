package com.lemnt.mall.application.checkout.service;

import com.lemnt.mall.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.mall.application.checkout.dto.CheckoutReceiptDTO;
import com.lemnt.mall.application.checkout.dto.SettlementAbandonDTO;
import com.lemnt.mall.application.checkout.dto.SettlementDTO;
import com.lemnt.mall.application.checkout.qry.CheckoutQuoteQry;
import com.lemnt.mall.application.checkout.service.CheckoutService;
import com.lemnt.mall.domain.checkout.gateway.SettlementGateway;
import com.lemnt.mall.domain.checkout.model.CheckoutRules;
import com.lemnt.mall.domain.order.gateway.OrderGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 结算服务。
 */
@Component
@RequiredArgsConstructor
public class SettlementService {
    private final CheckoutService checkoutService;
    private final OrderGateway orderPersistenceService;
    private final OrderPlacementService orderPlacementService;
    private final SettlementGateway settlementGateway;

    /**
     * 创建。
     */
    public SettlementDTO create(CheckoutQuoteQry query) {
        var value = orderPlacementService.prepareSettlement(query);
        settlementGateway.cache(value);
        return new SettlementDTO(
                value.id(), value.expiresAt(), CheckoutDTOConverter.quote(value.quote()));
    }

    /**
     * 提交。
     */
    public CheckoutReceiptDTO submit(Long memberId, String id, String requestNo) {
        var order = orderPlacementService.submitSettlement(memberId, id, requestNo, settlementGateway);
        return CheckoutDTOConverter.receipt(checkoutService.receipt(order.orderNo()), order);
    }

    /**
     * 查找按请求。
     */
    public CheckoutReceiptDTO findByRequest(Long memberId, String requestNo) {
        CheckoutRules.id(memberId);
        String key = CheckoutRules.text(requestNo, 64);
        var record = checkoutService.findRequest(
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
        return CheckoutDTOConverter.receipt(record, order);
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
                : CheckoutDTOConverter.receipt(checkoutService.receipt(order.orderNo()), order));
    }
}
