package com.lemnt.order.client.event;

import com.lemnt.order.client.dto.OrderStockLineDTO;

import java.io.Serializable;
import java.util.List;

/**
 * 已关闭订单的原预占资源，禁止使用当前商品数量重建释放计划。
 */
public record OrderClosedEvent(String orderNo, Long memberId, List<OrderStockLineDTO> items)
    implements Serializable {
}
