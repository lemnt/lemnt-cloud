package com.lemnt.trade.application.order.executor;

import com.lemnt.trade.application.order.cmd.OrderAdjustPriceCmd;
import com.lemnt.trade.application.order.service.OrderAdjustmentService;
import com.lemnt.trade.domain.order.model.TradeOrder;

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
