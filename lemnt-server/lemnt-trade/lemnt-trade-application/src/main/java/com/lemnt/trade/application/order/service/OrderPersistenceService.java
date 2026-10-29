package com.lemnt.trade.application.order.service;

import com.lemnt.trade.domain.order.model.TradeOperator;


import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.trade.domain.order.model.TradeOrder;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** OrderPersistenceService 跨表应用编排，保持原有事务和锁顺序。 */
@Component
@RequiredArgsConstructor
public class OrderPersistenceService {
    private final TradeOrderGateway tradeOrderGateway;
    private final TradeOrderItemGateway tradeOrderItemGateway;
    private final OrderLogService logs;

    /** 保存订单主体和明细，全部写入保持在一个事务内。 */
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder save(TradeOrder value) {
        var saved = tradeOrderGateway.saveHeader(value);
        tradeOrderItemGateway.saveItems(saved.id(), value.items());
        logs.record(saved, null, saved.status(), 1, TradeOperator.member(saved.memberId()), "用户下单");
        return saved;
    }

    /** 组合订单主体和明细。 */
    public TradeOrder findByOrderNo(String orderNo) {
        var value = tradeOrderGateway.findHeader(orderNo);
        return value == null ? null : new TradeOrder(value.id(),
                value.orderNo(),
                value.memberId(),
                value.totalAmount(),
                value.status(),
                tradeOrderItemGateway.items(value.id()),
                value.createTime(), value.usePoint(), value.givePoint(), value.delivery(), value.commentStatus(), value.finishTime());
    }

    /** 按原顺序锁定订单主体，调用方持有事务。 */
    public TradeOrder findByOrderNoForUpdate(String orderNo) {
        return tradeOrderGateway.lockHeader(orderNo);
    }
}
