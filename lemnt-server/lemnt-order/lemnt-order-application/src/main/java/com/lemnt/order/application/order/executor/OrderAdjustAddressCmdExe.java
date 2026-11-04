package com.lemnt.order.application.order.executor;

import com.lemnt.order.application.order.cmd.OrderAdjustAddressCmd;
import com.lemnt.order.application.order.service.OrderAdjustmentService;
import com.lemnt.order.domain.order.model.TradeOrder;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 后台改址执行器。
 */
@Component
@RequiredArgsConstructor
public class OrderAdjustAddressCmdExe {
    private final OrderAdjustmentService service;

    public TradeOrder execute(OrderAdjustAddressCmd cmd) {
        return service.adjustAddress(cmd);
    }
}
