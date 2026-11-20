package com.lemnt.order.domain.promotion.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单实际采用的优惠与权益结果，历史未知保持为空。
 */
public record OrderPromotion(
    Long id,
    Long orderId,
    String promotionKey,
    String promotionType,
    Long sourceId,
    String promotionName,
    String ruleSummary,
    String ruleSnapshot,
    BigDecimal discountAmount,
    BigDecimal deliveryDiscountPrice,
    Long usePoint,
    Long givePoint,
    Map<Long, Integer> giveCouponTemplateCounts,
    List<OrderItemPromotion> items) {
    public OrderPromotion {
        items = items == null ? List.of() : List.copyOf(items);
        giveCouponTemplateCounts =
            giveCouponTemplateCounts == null ? null : Map.copyOf(giveCouponTemplateCounts);
    }
}
