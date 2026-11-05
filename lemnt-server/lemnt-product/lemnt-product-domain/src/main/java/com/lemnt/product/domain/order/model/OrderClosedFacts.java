package com.lemnt.product.domain.order.model;

import java.util.List;

/**
 * 订单关闭事实（商品域视图）：触发按行取消预占。
 */
public record OrderClosedFacts(String orderNo, List<OrderStockLine> lines) {
}
