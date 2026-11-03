package com.lemnt.order.infrastructure.order.gateway;

import com.lemnt.order.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.order.domain.order.model.OrderRefundItem;
import com.lemnt.order.domain.order.model.TradeOrderItem;
import com.lemnt.order.infrastructure.order.converter.OrderItemConverter;
import com.lemnt.order.infrastructure.order.dataobject.TradeOrderItemDO;
import com.lemnt.order.infrastructure.order.mapper.TradeOrderItemMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** TradeOrderItem 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class TradeOrderItemGatewayImpl implements TradeOrderItemGateway {
    private final TradeOrderItemMapper tradeOrderItemMapper;

    @Override
    public List<OrderRefundItem> refundItems(Long orderId) {
        return tradeOrderItemMapper
                .selectList(
                        Wrappers.lambdaQuery(TradeOrderItemDO.class)
                                .eq(TradeOrderItemDO::getOrderId, orderId)
                                .orderByAsc(TradeOrderItemDO::getId))
                .stream()
                .map(
                        row -> {
                            var specs = OrderItemConverter.specs(row.getSpecs());
                            return new OrderRefundItem(
                                    row.getId(),
                                    row.getProductId(),
                                    row.getProductName(),
                                    row.getQuantity(),
                                    row.getLineAmount(),
                                    row.getPayAmount(),
                                    row.getDeliveryPrice(),
                                    row.getSpuId(),
                                    row.getSpuName(),
                                    specs == null
                                            ? null
                                            : specs.stream()
                                                    .map(
                                                            snapshotProperty ->
                                                                    new OrderRefundItem.Spec(
                                                                            snapshotProperty
                                                                                    .specId(),
                                                                            snapshotProperty
                                                                                    .specName(),
                                                                            snapshotProperty
                                                                                    .specValueId(),
                                                                            snapshotProperty
                                                                                    .specValueName()))
                                                    .toList(),
                                    row.getPicUrl());
                        })
                .toList();
    }

    /** 保存订单的明细集合。 */
    @Override
    public void saveItems(Long orderId, List<TradeOrderItem> items) {
        for (var item : items) {
            // 此入口只创建新订单明细，成功退款数量确定为0；纯转换器保留历史NULL。
            tradeOrderItemMapper.insert(
                    OrderItemConverter.toDO(orderId, item).setRefundedQuantity(0));
        }
    }

    /** 读取订单明细。 */
    @Override
    public List<TradeOrderItem> items(Long orderId) {
        return tradeOrderItemMapper
                .selectList(
                        Wrappers.<TradeOrderItemDO>lambdaQuery()
                                .eq(TradeOrderItemDO::getOrderId, orderId)
                                .orderByAsc(TradeOrderItemDO::getId))
                .stream()
                .map(OrderItemConverter::toModel)
                .toList();
    }

    @Override
    public boolean markCommented(Long orderId, Long orderItemId) {
        return tradeOrderItemMapper.update(
                        null,
                        Wrappers.lambdaUpdate(TradeOrderItemDO.class)
                                .eq(TradeOrderItemDO::getId, orderItemId)
                                .eq(TradeOrderItemDO::getOrderId, orderId)
                                .set(TradeOrderItemDO::getCommentStatus, true))
                == 1;
    }

    @Override
    public boolean updatePayAmount(Long orderId, Long orderItemId, BigDecimal payAmount) {
        var current = tradeOrderItemMapper.selectById(orderItemId);
        if (current == null || !orderId.equals(current.getOrderId())) {
            return false;
        }
        return tradeOrderItemMapper.update(
                        null,
                        Wrappers.lambdaUpdate(TradeOrderItemDO.class)
                                .eq(TradeOrderItemDO::getId, orderItemId)
                                .eq(TradeOrderItemDO::getOrderId, orderId)
                                .set(TradeOrderItemDO::getPayAmount, payAmount)
                                .set(
                                        current.getAdjustmentAmount() != null,
                                        TradeOrderItemDO::getAdjustmentAmount,
                                        current.getAdjustmentAmount() == null
                                                ? null
                                                : current.getAdjustmentAmount()
                                                        .add(
                                                                payAmount.subtract(
                                                                        current.getPayAmount()))))
                == 1;
    }

    @Override
    public boolean recordRefunded(Long orderId, Long orderItemId, int previous, int delta) {
        if (previous < 0 || delta < 1) {
            throw new IllegalArgumentException("已退数量不合法");
        }
        return tradeOrderItemMapper.update(
                        null,
                        Wrappers.<TradeOrderItemDO>lambdaUpdate()
                                .eq(TradeOrderItemDO::getOrderId, orderId)
                                .eq(TradeOrderItemDO::getId, orderItemId)
                                .ge(TradeOrderItemDO::getQuantity, Math.addExact(previous, delta))
                                .and(
                                        q ->
                                                q.eq(
                                                                TradeOrderItemDO
                                                                        ::getRefundedQuantity,
                                                                previous)
                                                        .or()
                                                        .isNull(
                                                                TradeOrderItemDO
                                                                        ::getRefundedQuantity))
                                .set(TradeOrderItemDO::getRefundedQuantity, previous + delta))
                == 1;
    }
}
