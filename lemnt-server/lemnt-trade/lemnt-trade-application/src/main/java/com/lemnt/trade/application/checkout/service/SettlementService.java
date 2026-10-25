package com.lemnt.trade.application.checkout.service;

import com.lemnt.trade.domain.checkout.gateway.SettlementGateway;
import com.lemnt.trade.application.checkout.qry.CheckoutQuoteQry;
import com.lemnt.trade.application.checkout.dto.CheckoutReceiptDTO;
import com.lemnt.trade.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.trade.domain.checkout.gateway.CheckoutGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementService {
    private final OrderPlacementService placement;
    private final SettlementGateway settlements;
    private final CheckoutGateway checkout;
    private final com.lemnt.trade.domain.gateway.TradeOrderGateway orders;

    public com.lemnt.trade.application.checkout.dto.SettlementDTO create(CheckoutQuoteQry query) {
        var value = placement.prepareSettlement(query);
        settlements.cache(value);
        return new com.lemnt.trade.application.checkout.dto.SettlementDTO(
                value.id(), value.expiresAt(), CheckoutDTOConverter.quote(value.quote()));
    }

    public CheckoutReceiptDTO submit(Long memberId, String id, String requestNo) {
        var order = placement.submitSettlement(memberId, id, requestNo, settlements);
        return CheckoutDTOConverter.receipt(checkout.receipt(order.orderNo()), order);
    }

    public CheckoutReceiptDTO findByRequest(Long memberId, String requestNo) {
        com.lemnt.trade.domain.checkout.model.CheckoutRules.id(memberId);
        String key = com.lemnt.trade.domain.checkout.model.CheckoutRules.text(requestNo, 64);
        var record = checkout.findRequest(memberId, key);
        if (record == null || !memberId.equals(record.memberId())) return null;
        var order = orders.findByOrderNo(record.orderNo());
        if (order == null || !memberId.equals(order.memberId())) return null;
        return CheckoutDTOConverter.receipt(record, order);
    }

    public com.lemnt.trade.application.checkout.dto.SettlementAbandonDTO abandon(
            Long memberId, String id, String requestNo) {
        var order = placement.abandonSettlement(memberId, id, requestNo, settlements);
        return new com.lemnt.trade.application.checkout.dto.SettlementAbandonDTO(
                order == null,
                order == null
                        ? null
                        : CheckoutDTOConverter.receipt(checkout.receipt(order.orderNo()), order));
    }
}
