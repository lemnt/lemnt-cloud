package com.lemnt.order.application.order.executor;

import com.lemnt.order.application.order.cmd.OrderAdjustPriceCmd;
import com.lemnt.order.application.order.service.OrderAdjustmentService;
import com.lemnt.order.domain.order.model.TradeOrder;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 后台调价执行器。 */
@Component
@RequiredArgsConstructor
public class OrderAdjustPriceCmdExe {
    private final OrderAdjustmentService service;

    public TradeOrder execute(OrderAdjustPriceCmd cmd) {
        return service.adjustPrice(cmd);
    }
}
