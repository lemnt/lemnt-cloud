package com.lemnt.trade.domain.promotion.model;

/** 生成售后时冻结的优惠回退归因，历史未知保持为空。 */
public record OrderPromotionRefund(
        Long id,
        Long afterSaleId,
        Long orderItemPromotionId,
        java.math.BigDecimal refundDiscountAmount,
        java.math.BigDecimal refundDeliveryDiscountPrice,
        Long returnUsePoint,
        Long deductGivePoint) {}
