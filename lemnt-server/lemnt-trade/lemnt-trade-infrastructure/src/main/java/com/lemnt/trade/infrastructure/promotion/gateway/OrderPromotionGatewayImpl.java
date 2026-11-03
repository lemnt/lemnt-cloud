package com.lemnt.trade.infrastructure.promotion.gateway;

import com.lemnt.trade.domain.order.model.TradeOrderItem;
import com.lemnt.trade.domain.promotion.gateway.OrderPromotionGateway;
import com.lemnt.trade.domain.promotion.model.*;
import com.lemnt.trade.domain.promotion.model.OrderGiftAdjustment;
import com.lemnt.trade.infrastructure.order.dataobject.TradeOrderDO;
import com.lemnt.trade.infrastructure.order.dataobject.TradeOrderItemDO;
import com.lemnt.trade.infrastructure.order.mapper.TradeOrderItemMapper;
import com.lemnt.trade.infrastructure.order.mapper.TradeOrderMapper;
import com.lemnt.trade.infrastructure.promotion.converter.OrderPromotionConverter;
import com.lemnt.trade.infrastructure.promotion.dataobject.*;
import com.lemnt.trade.infrastructure.promotion.mapper.*;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;

/** 三张优惠扩展表与订单使用同一个数据源及事务。 */
@Component
@RequiredArgsConstructor
public class OrderPromotionGatewayImpl implements OrderPromotionGateway {
    private final OrderPromotionMapper promotions;
    private final OrderItemPromotionMapper allocations;
    private final OrderPromotionRefundMapper refunds;
    private final TradeOrderItemMapper orderItems;
    private final TradeOrderMapper orders;

    @Override
    public void save(Long orderId, List<OrderPromotion> values, List<TradeOrderItem> items) {
        for (var value : values) {
            var row = OrderPromotionConverter.toDO(value).setId(null).setOrderId(orderId);
            promotions.insert(row);
            for (var item : value.items()) {
                int position;
                try {
                    position = Integer.parseInt(item.quoteLineKey());
                } catch (RuntimeException exception) {
                    throw new IllegalArgumentException("优惠商品行标识不合法");
                }
                if (position < 0
                        || position >= items.size()
                        || items.get(position).orderItemId() == null) {
                    throw new IllegalArgumentException("优惠商品行不存在");
                }
                allocations.insert(
                        OrderPromotionConverter.toDO(item)
                                .setId(null)
                                .setOrderPromotionId(row.getId())
                                .setOrderItemId(items.get(position).orderItemId()));
            }
        }
    }

    @Override
    public List<OrderPromotion> find(Long orderId) {
        var headers =
                promotions.selectList(
                        Wrappers.<OrderPromotionDO>lambdaQuery()
                                .eq(OrderPromotionDO::getOrderId, orderId)
                                .orderByAsc(OrderPromotionDO::getId));
        if (headers.isEmpty()) {
            return List.of();
        }
        var positions = new HashMap<Long, String>();
        var lines =
                orderItems.selectList(
                        Wrappers.<TradeOrderItemDO>lambdaQuery()
                                .eq(TradeOrderItemDO::getOrderId, orderId)
                                .orderByAsc(TradeOrderItemDO::getId));
        for (int i = 0; i < lines.size(); i++) {
            positions.put(lines.get(i).getId(), String.valueOf(i));
        }
        var children =
                allocations.selectList(
                        Wrappers.<OrderItemPromotionDO>lambdaQuery()
                                .in(
                                        OrderItemPromotionDO::getOrderPromotionId,
                                        headers.stream().map(OrderPromotionDO::getId).toList())
                                .orderByAsc(OrderItemPromotionDO::getId));
        return headers.stream()
                .map(
                        row ->
                                OrderPromotionConverter.from(
                                        row,
                                        children.stream()
                                                .filter(
                                                        line ->
                                                                line.getOrderPromotionId()
                                                                        .equals(row.getId()))
                                                .map(
                                                        line ->
                                                                OrderPromotionConverter.from(
                                                                        line,
                                                                        positions.get(
                                                                                line
                                                                                        .getOrderItemId())))
                                                .toList()))
                .toList();
    }

    @Override
    public void saveRefund(Long afterSaleId, List<OrderPromotionRefund> values) {
        for (var value : values) {
            refunds.insert(
                    OrderPromotionConverter.toDO(value).setId(null).setAfterSaleId(afterSaleId));
        }
    }

    @Override
    public List<OrderPromotionRefund> refunds(List<Long> afterSaleIds) {
        if (afterSaleIds.isEmpty()) {
            return List.of();
        }
        return refunds
                .selectList(
                        Wrappers.<OrderPromotionRefundDO>lambdaQuery()
                                .in(OrderPromotionRefundDO::getAfterSaleId, afterSaleIds)
                                .orderByAsc(OrderPromotionRefundDO::getId))
                .stream()
                .map(OrderPromotionConverter::from)
                .toList();
    }

    @Override
    public void updateGift(OrderGiftAdjustment value) {
        if (orders.update(
                                null,
                                Wrappers.<TradeOrderDO>lambdaUpdate()
                                        .eq(TradeOrderDO::getId, value.orderId())
                                        .eq(TradeOrderDO::getStatus, "PENDING_PAYMENT")
                                        .set(TradeOrderDO::getGivePoint, value.givePoint()))
                        != 1
                || promotions.update(
                                null,
                                Wrappers.<OrderPromotionDO>lambdaUpdate()
                                        .eq(OrderPromotionDO::getId, value.promotionId())
                                        .eq(OrderPromotionDO::getOrderId, value.orderId())
                                        .eq(OrderPromotionDO::getPromotionType, "ORDER_GIFT")
                                        .set(
                                                OrderPromotionDO::getGivePoint,
                                                value.promotionGivePoint()))
                        != 1) {
            throw new IllegalStateException("调价赠积分头更新冲突");
        }
        value.itemGivePoints()
                .forEach(
                        (id, points) -> {
                            if (orderItems.update(
                                            null,
                                            Wrappers.<TradeOrderItemDO>lambdaUpdate()
                                                    .eq(TradeOrderItemDO::getId, id)
                                                    .eq(
                                                            TradeOrderItemDO::getOrderId,
                                                            value.orderId())
                                                    .set(TradeOrderItemDO::getGivePoint, points))
                                    != 1) {
                                throw new IllegalStateException("调价赠积分明细更新冲突");
                            }
                        });
        value.allocationGivePoints()
                .forEach(
                        (id, points) -> {
                            if (allocations.update(
                                            null,
                                            Wrappers.<OrderItemPromotionDO>lambdaUpdate()
                                                    .eq(OrderItemPromotionDO::getId, id)
                                                    .eq(
                                                            OrderItemPromotionDO
                                                                    ::getOrderPromotionId,
                                                            value.promotionId())
                                                    .set(
                                                            OrderItemPromotionDO::getGivePoint,
                                                            points))
                                    != 1) {
                                throw new IllegalStateException("调价赠积分分摊更新冲突");
                            }
                        });
    }
}
