package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.cmd.*;
import com.lemnt.trade.application.fulfillment.dto.*;
import com.lemnt.trade.application.fulfillment.converter.FulfillmentDTOConverter;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import com.lemnt.trade.domain.fulfillment.gateway.FulfillmentGateway;
import com.lemnt.trade.domain.checkout.model.CheckoutRules;

@Component
@RequiredArgsConstructor
public class RefundDetailQueryExe {
    private final FulfillmentGateway fulfillment;

    public RefundRequestDTO execute(String no) {
        return FulfillmentDTOConverter.refund(fulfillment.refund(CheckoutRules.text(no, 40)));
    }
}
