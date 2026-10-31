package com.lemnt.mall.domain.promotion.model;

/** 活动订单商品行，保存该行消耗积分和累计退款数量。 */
public record PromotionOrderItem(
        Long id,
        String orderNo,
        Long skuId,
        Integer quantity,
        Long usePoint,
        Integer refundedQuantity
) implements java.io.Serializable {
}
