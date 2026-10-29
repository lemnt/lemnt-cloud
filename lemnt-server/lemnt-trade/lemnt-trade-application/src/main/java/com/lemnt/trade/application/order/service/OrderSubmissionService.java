package com.lemnt.trade.application.order.service;

import com.lemnt.trade.domain.order.model.TradeOrder;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

/** 订单与明细的本地持久化事务，库存由调用方在提交后同步预占。 */
@Component
@RequiredArgsConstructor
public class OrderSubmissionService {
    private final OrderPersistenceService orders;

    /** 重试使用同一个订单号，订单内容不一致时拒绝。 */
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder create(TradeOrder order) {
        var previous = orders.findByOrderNoForUpdate(order.orderNo());
        if (previous != null
        ) {
            previous = orders.findByOrderNo(order.orderNo());
            requireSame(previous, order);
            return previous;
        }
        var saved = orders.save(order);
        return saved;
    }

    /** 验证幂等重试没有替换会员、金额或商品快照。 */
    public void requireSame(TradeOrder previous, TradeOrder order) {
        if (!previous.memberId().equals(order.memberId())
                || previous.totalAmount().compareTo(order.totalAmount()) != 0
                || previous.usePoint() != order.usePoint() || previous.givePoint() != order.givePoint()
                || !Objects.equals(previous.delivery(), order.delivery())
                || !purchaseItems(previous.items()).equals(purchaseItems(order.items()))
        ) {
            throw new IllegalStateException("订单号已用于不同的订单内容");
        }
    }
    /** 数据库主键及评价状态不属于原购买快照，不能影响下单原请求重试。 */
    private java.util.List<com.lemnt.trade.domain.order.model.TradeOrderItem> purchaseItems(
            java.util.List<com.lemnt.trade.domain.order.model.TradeOrderItem> values) {
        return values.stream().map(item -> new com.lemnt.trade.domain.order.model.TradeOrderItem(
                item.productId(), item.productCode(), item.productName(), item.unitPrice(), item.quantity(),
                item.lineAmount(), item.payAmount(), item.deliveryPrice(), item.spuId(), item.spuName(),
                item.properties(), item.picUrl())).toList();
    }
}
