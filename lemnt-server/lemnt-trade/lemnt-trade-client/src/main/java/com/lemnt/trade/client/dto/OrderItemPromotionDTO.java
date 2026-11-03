package com.lemnt.trade.client.dto;

/** 已确认的优惠结果，跨边界只传冻结值。 */
public record OrderItemPromotionDTO(
        Long id,
        Long orderPromotionId,
        Long orderItemId,
        String quoteLineKey,
        java.math.BigDecimal discountAmount,
        java.math.BigDecimal deliveryDiscountPrice,
        Long usePoint,
        Long givePoint)
        implements java.io.Serializable {}
