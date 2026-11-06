package com.lemnt.order.domain.promotion.model;

import java.math.BigDecimal;

/**
 * 优惠在原订单商品明细上的分摊，历史未知保持为空。
 */
public record OrderItemPromotion(
    Long id,
    Long orderPromotionId,
    Long orderItemId,
    String quoteLineKey,
    BigDecimal discountAmount,
    BigDecimal deliveryDiscountPrice,
    Long usePoint,
    Long givePoint) {
}
