package com.lemnt.trade.application.order.executor;

import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.order.model.TradeOrder;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 交易订单详情查询执行器。
 */
@Component
@RequiredArgsConstructor
public class TradeOrderDetailQueryExe {
    private final OrderPersistenceService orderPersistenceService;

    /**
     * 执行交易订单详情查询。
     */
    public TradeOrder execute(String no) {
        if(no==null||no.isBlank()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        return orderPersistenceService.findByOrderNo(no.trim());
    }
}
