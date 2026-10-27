package com.lemnt.trade.domain.order.gateway;

import com.lemnt.trade.domain.order.model.TradeOrderItem;

import java.util.List;

/** TradeOrderItem 表领域存储接口。 */
public interface TradeOrderItemGateway {
    /** 保存订单的明细集合。 */
    void saveItems(Long orderId, List<TradeOrderItem> items);

    /** 读取订单明细。 */
    List<TradeOrderItem> items(Long orderId);
}
