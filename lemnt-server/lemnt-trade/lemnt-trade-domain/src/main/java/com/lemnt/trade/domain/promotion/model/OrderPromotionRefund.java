package com.lemnt.trade.domain.promotion.model;

import java.math.BigDecimal;

/** 生成售后时冻结的优惠回退归因，历史未知保持为空。 */
public record OrderPromotionRefund(
        Long id,
        Long afterSaleId,
        Long orderItemPromotionId,
        BigDecimal refundDiscountAmount,
        BigDecimal refundDeliveryDiscountPrice,
        Long returnUsePoint,
        Long deductGivePoint) {}
