package com.lemnt.order.application.promotion.cmd;

/** 活动订单商品行。 */
public record PromotionOrderLineCmd(Long skuId, Integer quantity) {}
