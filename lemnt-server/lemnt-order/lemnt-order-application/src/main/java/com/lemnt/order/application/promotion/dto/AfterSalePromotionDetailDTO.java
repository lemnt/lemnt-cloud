package com.lemnt.order.application.promotion.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 冻结优惠查询结果，历史未知不读取现值补写。
 */
public record AfterSalePromotionDetailDTO(
    String afterSaleNo,
    Long orderItemId,
    Integer quantity,
    BigDecimal amount,
    BigDecimal discountAmount,
    BigDecimal deliveryDiscountPrice,
    Long returnUsePoint,
    Long deductGivePoint,
    Integer ruleVersion,
    String snapshotStatus,
    String benefitStatus,
    List<Promotion> promotions)
    implements Serializable {
    public record Promotion(
        String promotionType,
        String promotionName,
        BigDecimal discountAmount,
        BigDecimal deliveryDiscountPrice,
        Long returnUsePoint,
        Long deductGivePoint)
        implements Serializable {
    }
}
