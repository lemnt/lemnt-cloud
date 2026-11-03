package com.lemnt.trade.application.promotion.dto;

/** 冻结优惠查询结果，历史未知不读取现值补写。 */
public record OrderPromotionDetailDTO(
        String orderNo,
        String snapshotStatus,
        Summary summary,
        java.util.List<Promotion> promotions)
        implements java.io.Serializable {
    public record Summary(
            java.math.BigDecimal originalAmount,
            java.math.BigDecimal discountAmount,
            java.math.BigDecimal deliveryOriginalPrice,
            java.math.BigDecimal deliveryDiscountPrice,
            java.math.BigDecimal adjustmentAmount,
            java.math.BigDecimal payAmount)
            implements java.io.Serializable {}

    public record Promotion(
            String promotionType,
            String promotionName,
            java.math.BigDecimal discountAmount,
            java.math.BigDecimal deliveryDiscountPrice,
            Long usePoint,
            Long givePoint,
            java.util.Map<Long, Integer> giveCouponTemplateCounts,
            java.util.List<Item> items)
            implements java.io.Serializable {}

    public record Item(
            Long orderItemId,
            java.math.BigDecimal discountAmount,
            java.math.BigDecimal deliveryDiscountPrice,
            Long usePoint,
            Long givePoint)
            implements java.io.Serializable {}
}
