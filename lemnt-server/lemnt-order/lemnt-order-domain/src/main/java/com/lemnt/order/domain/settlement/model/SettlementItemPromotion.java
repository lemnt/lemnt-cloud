package com.lemnt.order.domain.settlement.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 已确认的优惠结果，跨边界只传冻结值。 */
public record SettlementItemPromotion(
        Long id,
        Long orderPromotionId,
        Long orderItemId,
        String quoteLineKey,
        BigDecimal discountAmount,
        BigDecimal deliveryDiscountPrice,
        Long usePoint,
        Long givePoint)
        implements Serializable {}
