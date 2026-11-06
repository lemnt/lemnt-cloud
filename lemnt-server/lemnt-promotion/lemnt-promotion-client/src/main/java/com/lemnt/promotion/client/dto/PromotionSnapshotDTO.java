package com.lemnt.promotion.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 已确认的优惠结果，跨边界只传冻结值。
 */
public record PromotionSnapshotDTO(
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
    List<PromotionItemSnapshotDTO> items)
    implements Serializable {
    public PromotionSnapshotDTO {
        items = items == null ? List.of() : List.copyOf(items);
        giveCouponTemplateCounts =
            giveCouponTemplateCounts == null ? null : Map.copyOf(giveCouponTemplateCounts);
    }
}
