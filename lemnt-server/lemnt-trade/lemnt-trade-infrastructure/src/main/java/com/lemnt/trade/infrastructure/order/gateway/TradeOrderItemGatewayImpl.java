package com.lemnt.trade.infrastructure.order.gateway;

import com.lemnt.trade.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.trade.domain.order.model.TradeOrderItem;
import com.lemnt.trade.infrastructure.order.dataobject.TradeOrderItemDO;
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

    /** 保存订单的明细集合。 */
    @Override
    public void saveItems(Long orderId, List<TradeOrderItem> items) {
        for (var item : items) {
            tradeOrderItemMapper.insert(new TradeOrderItemDO().setOrderId(orderId).setProductId(item.productId()).setProductCode(item.productCode()).setProductName(item.productName()).setUnitPrice(item.unitPrice()).setQuantity(item.quantity()).setLineAmount(item.lineAmount()));
        }
    }

    /** 读取订单明细。 */
    @Override
    public List<TradeOrderItem> items(Long orderId) {
        return tradeOrderItemMapper.selectList(Wrappers.lambdaQuery(TradeOrderItemDO.class).eq(TradeOrderItemDO::getOrderId,

                orderId).orderByAsc(TradeOrderItemDO::getId)).stream().map(item -> new TradeOrderItem(item.getProductId(),
                item.getProductCode(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getLineAmount())).toList();
    }
}
