package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.cmd.RefundRejectCmd;
import com.lemnt.trade.application.fulfillment.converter.FulfillmentDTOConverter;
import com.lemnt.trade.application.fulfillment.dto.RefundRequestDTO;
import com.lemnt.trade.application.fulfillment.service.OrderFulfillmentService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 退款拒绝命令执行器。
 */
@Component
@RequiredArgsConstructor
public class RefundRejectCmdExe {

    private final OrderFulfillmentService orderFulfillmentService;

    /**
     * 执行退款拒绝命令。
     */
    public RefundRequestDTO execute(RefundRejectCmd cmd) {
        return FulfillmentDTOConverter.refund(orderFulfillmentService.rejectRefund(cmd));
    }
}
