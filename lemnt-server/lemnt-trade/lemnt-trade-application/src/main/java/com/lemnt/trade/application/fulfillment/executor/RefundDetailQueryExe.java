package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.converter.FulfillmentDTOConverter;
import com.lemnt.trade.application.fulfillment.dto.RefundRequestDTO;
import com.lemnt.trade.domain.fulfillment.gateway.RefundRequestGateway;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 退款详情查询执行器。
 */
@Component
@RequiredArgsConstructor
public class RefundDetailQueryExe {
    private final RefundRequestGateway refundRequestGateway;

    /**
     * 执行退款详情查询。
     */
    public RefundRequestDTO execute(String no) {
        return FulfillmentDTOConverter.refund(refundRequestGateway.refund(OrderRules.text(no, 40)));
    }
}
