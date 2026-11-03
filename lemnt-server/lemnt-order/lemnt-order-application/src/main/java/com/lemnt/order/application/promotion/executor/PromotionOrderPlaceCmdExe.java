package com.lemnt.order.application.promotion.executor;

import com.lemnt.order.application.promotion.cmd.PromotionOrderCmd;
import com.lemnt.order.application.promotion.service.PromotionOrderService;
import com.lemnt.order.domain.settlement.model.SettlementOrder;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 活动订单下单执行器。 */
@Component
@RequiredArgsConstructor
public class PromotionOrderPlaceCmdExe {
    private final PromotionOrderService service;

    public SettlementOrder execute(PromotionOrderCmd cmd) {
        return service.place(cmd);
    }
}
