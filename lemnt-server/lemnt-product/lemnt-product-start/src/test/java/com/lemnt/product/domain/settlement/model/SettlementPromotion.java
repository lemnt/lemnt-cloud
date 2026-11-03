package com.lemnt.product.domain.settlement.model;

/** 已确认的优惠结果，跨边界只传冻结值。 */
public record SettlementPromotion(
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
        java.util.List<SettlementItemPromotion> items)
        implements java.io.Serializable {
    public SettlementPromotion {
        items = items == null ? java.util.List.of() : java.util.List.copyOf(items);
        giveCouponTemplateCounts =
                giveCouponTemplateCounts == null
                        ? null
                        : java.util.Map.copyOf(giveCouponTemplateCounts);
    }
}
