package com.lemnt.trade.infrastructure.order.gateway;

import com.lemnt.trade.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.trade.domain.order.model.OrderRefundItem;
import com.lemnt.trade.domain.order.model.TradeOrderItem;
import com.lemnt.trade.infrastructure.order.dataobject.TradeOrderItemDO;
import com.lemnt.trade.infrastructure.order.converter.OrderItemConverter;
import com.lemnt.trade.infrastructure.order.mapper.TradeOrderItemMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** TradeOrderItem 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class TradeOrderItemGatewayImpl implements TradeOrderItemGateway {
    private final TradeOrderItemMapper tradeOrderItemMapper;

    @Override
    public List<OrderRefundItem> refundItems(Long orderId) {
        return tradeOrderItemMapper.selectList(Wrappers
                .lambdaQuery(TradeOrderItemDO.class)
                .eq(TradeOrderItemDO::getOrderId, orderId)
                .orderByAsc(TradeOrderItemDO::getId))
                .stream().map(row -> new OrderRefundItem(row.getId(), row.getProductId(), row.getProductName(),
                        row.getQuantity(), row.getLineAmount(), row.getPayAmount(), row.getDeliveryPrice(), row.getSpuId(),
                        row.getSpuName(),
                OrderItemConverter.properties(row.getProperties()) == null ? null : OrderItemConverter.properties(row.getProperties()).stream().map(snapshotProperty ->
                        new OrderRefundItem.Property(snapshotProperty.propertyId(), snapshotProperty.propertyName(),
                                snapshotProperty.valueId(), snapshotProperty.valueName())).toList(), row.getPicUrl())).toList();
    }

    /** 保存订单的明细集合。 */
    @Override
    public void saveItems(Long orderId, List<TradeOrderItem> items) {
        for (var item : items
        ) {
            tradeOrderItemMapper.insert(OrderItemConverter.toDO(orderId, item));
        }
    }

    /** 读取订单明细。 */
    @Override
    public List<TradeOrderItem> items(Long orderId) {
        return tradeOrderItemMapper.selectList(Wrappers.<TradeOrderItemDO>lambdaQuery()
                .eq(TradeOrderItemDO::getOrderId, orderId)
                .orderByAsc(TradeOrderItemDO::getId)).stream().map(OrderItemConverter::toModel).toList();
    }
}
