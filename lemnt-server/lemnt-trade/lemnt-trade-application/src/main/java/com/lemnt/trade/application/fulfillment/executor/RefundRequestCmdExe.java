package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.cmd.RefundRequestCmd;
import com.lemnt.trade.application.fulfillment.converter.FulfillmentDTOConverter;
import com.lemnt.trade.application.fulfillment.dto.RefundRequestDTO;
import com.lemnt.trade.application.fulfillment.service.OrderFulfillmentService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 退款请求命令执行器。
 */
@Component
@RequiredArgsConstructor
public class RefundRequestCmdExe {

    private final OrderFulfillmentService orderFulfillmentService;

    /**
     * 执行退款请求命令。
     */
    public RefundRequestDTO execute(RefundRequestCmd cmd) {
        return FulfillmentDTOConverter.refund(orderFulfillmentService.requestRefund(cmd));
    }
}
