package com.lemnt.trade.application.promotion.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 冻结优惠查询结果，历史未知不读取现值补写。 */
public record OrderPromotionDetailDTO(
        String orderNo, String snapshotStatus, Summary summary, List<Promotion> promotions)
        implements Serializable {
    public record Summary(
            BigDecimal originalAmount,
            BigDecimal discountAmount,
            BigDecimal deliveryOriginalPrice,
            BigDecimal deliveryDiscountPrice,
            BigDecimal adjustmentAmount,
            BigDecimal payAmount)
            implements Serializable {}

    public record Promotion(
            String promotionType,
            String promotionName,
            BigDecimal discountAmount,
            BigDecimal deliveryDiscountPrice,
            Long usePoint,
            Long givePoint,
            Map<Long, Integer> giveCouponTemplateCounts,
            List<Item> items)
            implements Serializable {}

    public record Item(
            Long orderItemId,
            BigDecimal discountAmount,
            BigDecimal deliveryDiscountPrice,
            Long usePoint,
            Long givePoint)
            implements Serializable {}
}
