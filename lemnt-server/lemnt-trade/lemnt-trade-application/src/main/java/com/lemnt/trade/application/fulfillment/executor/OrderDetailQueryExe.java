package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.cmd.*;
import com.lemnt.trade.application.fulfillment.dto.*;
import com.lemnt.trade.application.fulfillment.converter.FulfillmentDTOConverter;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import com.lemnt.trade.domain.fulfillment.gateway.FulfillmentGateway;
import com.lemnt.trade.domain.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.checkout.model.CheckoutRules;

@Component
@RequiredArgsConstructor
public class OrderDetailQueryExe {
    private final TradeOrderGateway orders;
    private final FulfillmentGateway fulfillment;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public OrderDetailDTO execute(String value) {
        String no = CheckoutRules.text(value, 40);
        var order = orders.findByOrderNo(no);
        return order == null
                ? null
                : new OrderDetailDTO(
                        no,
                        order.memberId(),
                        order.totalAmount(),
                        order.status(),
                        fulfillment.inventoryConfirmed(no),
                        fulfillment.activeRefund(no),
                        order.items().stream()
                                .map(
                                        com.lemnt.trade.application.checkout.converter
                                                        .CheckoutDTOConverter
                                                ::item)
                                .toList(),
                        FulfillmentDTOConverter.shipment(fulfillment.shipment(no)));
    }
}
