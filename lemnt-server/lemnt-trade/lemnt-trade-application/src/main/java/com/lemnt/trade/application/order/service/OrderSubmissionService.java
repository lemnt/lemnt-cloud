package com.lemnt.trade.application.order.service;

import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.outbox.gateway.TradeOutboxGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

/** 订单、明细及库存预占事件在交易库内原子提交。 */
@Component
@RequiredArgsConstructor
public class OrderSubmissionService {
    private final OrderPersistenceService orders;
    private final TradeOutboxGateway outbox;

    /** 重试使用同一个订单号，订单内容不一致时拒绝。 */
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder create(TradeOrder order) {
        var previous = orders.findByOrderNoForUpdate(order.orderNo());
        if (previous != null) {
            previous = orders.findByOrderNo(order.orderNo());
            requireSame(previous, order);
            return previous;
        }
        var saved = orders.save(order);
        outbox.append(UUID.randomUUID().toString(), "TRADE_ORDER", order.orderNo(),
                "TRADE_ORDER_RESERVE", "{}");
        return saved;
    }

    /** 验证幂等重试没有替换会员、金额或商品快照。 */
    public void requireSame(TradeOrder previous, TradeOrder order) {
        if (!previous.memberId().equals(order.memberId())
                || previous.totalAmount().compareTo(order.totalAmount()) != 0
                || !previous.items().equals(order.items())) {
            throw new IllegalStateException("订单号已用于不同的订单内容");
        }
    }
}
