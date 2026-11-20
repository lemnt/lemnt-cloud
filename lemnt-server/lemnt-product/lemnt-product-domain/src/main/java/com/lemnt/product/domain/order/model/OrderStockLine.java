package com.lemnt.product.domain.order.model;

/**
 * 订单库存行的商品域视图。
 */
public record OrderStockLine(Long skuId, int quantity) {
}
