package com.lemnt.trade.domain.promotion.model;

/** 订单实际采用的优惠与权益结果，历史未知保持为空。 */
public record OrderPromotion(
        Long id,
        Long orderId,
        String promotionKey,
        String promotionType,
        Long sourceId,
        String promotionName,
        String ruleSummary,
        String ruleSnapshot,
        java.math.BigDecimal discountAmount,
        java.math.BigDecimal deliveryDiscountPrice,
        Long usePoint,
        Long givePoint,
        java.util.Map<Long, Integer> giveCouponTemplateCounts,
        java.util.List<OrderItemPromotion> items) {
    public OrderPromotion {
        items = items == null ? java.util.List.of() : java.util.List.copyOf(items);
        giveCouponTemplateCounts =
                giveCouponTemplateCounts == null
                        ? null
                        : java.util.Map.copyOf(giveCouponTemplateCounts);
    }
}
