package com.lemnt.trade.domain.promotion.model;

/** 优惠在原订单商品明细上的分摊，历史未知保持为空。 */
public record OrderItemPromotion(
        Long id,
        Long orderPromotionId,
        Long orderItemId,
        String quoteLineKey,
        java.math.BigDecimal discountAmount,
        java.math.BigDecimal deliveryDiscountPrice,
        Long usePoint,
        Long givePoint) {}
