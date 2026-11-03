package com.lemnt.mall.domain.promotion.model;

import java.io.Serializable;

/** 活动订单商品行，保存该行消耗积分和累计退款数量。 */
public record PromotionOrderItem(
        Long id,
        String orderNo,
        Long skuId,
        Integer quantity,
        Long usePoint,
        Integer refundedQuantity)
        implements Serializable {}
