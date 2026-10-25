package com.lemnt.trade.application.checkout.executor;

import com.lemnt.trade.application.checkout.cmd.*;
import com.lemnt.trade.application.checkout.dto.*;
import com.lemnt.trade.application.checkout.converter.CheckoutDTOConverter;
import com.lemnt.trade.domain.checkout.gateway.CheckoutGateway;
import com.lemnt.trade.domain.checkout.model.*;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CheckoutSubmitCmdExe {
    private final com.lemnt.trade.application.checkout.service.OrderPlacementService placement;
    private final CheckoutGateway checkout;

    public CheckoutReceiptDTO execute(CheckoutSubmitCmd cmd) {
        var order = placement.checkoutCart(cmd);
        return CheckoutDTOConverter.receipt(checkout.receipt(order.orderNo()), order);
    }
}
