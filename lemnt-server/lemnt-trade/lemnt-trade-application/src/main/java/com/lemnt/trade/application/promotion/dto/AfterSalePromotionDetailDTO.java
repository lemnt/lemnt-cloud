package com.lemnt.trade.application.promotion.dto;

/** 冻结优惠查询结果，历史未知不读取现值补写。 */
public record AfterSalePromotionDetailDTO(
        String afterSaleNo,
        Long orderItemId,
        Integer quantity,
        java.math.BigDecimal amount,
        java.math.BigDecimal discountAmount,
        java.math.BigDecimal deliveryDiscountPrice,
        Long returnUsePoint,
        Long deductGivePoint,
        Integer ruleVersion,
        String snapshotStatus,
        String benefitStatus,
        java.util.List<Promotion> promotions)
        implements java.io.Serializable {
    public record Promotion(
            String promotionType,
            String promotionName,
            java.math.BigDecimal discountAmount,
            java.math.BigDecimal deliveryDiscountPrice,
            Long returnUsePoint,
            Long deductGivePoint)
            implements java.io.Serializable {}
}
