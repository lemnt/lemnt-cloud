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
}
