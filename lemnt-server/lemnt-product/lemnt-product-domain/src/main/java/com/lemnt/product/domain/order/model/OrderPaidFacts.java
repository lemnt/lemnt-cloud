package com.lemnt.product.domain.order.model;

import java.util.List;

/**
 * 订单支付事实（商品域视图）：orderNo为预占台账键，lines为该订单的SKU扣减行。
 */
public record OrderPaidFacts(String orderNo, List<OrderStockLine> lines) {
}
