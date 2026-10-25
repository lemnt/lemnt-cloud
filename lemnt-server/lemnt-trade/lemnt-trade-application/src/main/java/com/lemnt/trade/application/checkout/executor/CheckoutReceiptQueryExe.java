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
public class CheckoutReceiptQueryExe {
    private final CheckoutGateway checkout;
    private final com.lemnt.trade.domain.gateway.TradeOrderGateway orders;

    public CheckoutReceiptDTO execute(String orderNo) {
        String key = CheckoutRules.text(orderNo, 40);
        return CheckoutDTOConverter.receipt(checkout.receipt(key), orders.findByOrderNo(key));
    }

    public CheckoutReceiptDTO execute(Long memberId, String orderNo) {
        CheckoutRules.id(memberId);
        String key = CheckoutRules.text(orderNo, 40);
        var order = orders.findByOrderNo(key);
        if (order == null || !memberId.equals(order.memberId())) return null;
        var record = checkout.receipt(key);
        if (record == null || !memberId.equals(record.memberId())) return null;
        return CheckoutDTOConverter.receipt(record, order);
    }
}
