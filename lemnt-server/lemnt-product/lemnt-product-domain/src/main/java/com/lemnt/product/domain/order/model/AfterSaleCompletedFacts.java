package com.lemnt.product.domain.order.model;

/**
 * 售后完成事实（商品域视图）：按件回补SKU库存，afterSaleNo保证重试幂等。
 */
public record AfterSaleCompletedFacts(
    String orderNo, String afterSaleNo, Long skuId, Integer quantity, int ruleVersion) {
}
