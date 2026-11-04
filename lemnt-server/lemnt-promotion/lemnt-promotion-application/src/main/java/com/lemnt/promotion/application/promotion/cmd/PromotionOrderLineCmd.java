package com.lemnt.promotion.application.promotion.cmd;

/**
 * 活动订单商品行。
 */
public record PromotionOrderLineCmd(Long skuId, Integer quantity) {
}
