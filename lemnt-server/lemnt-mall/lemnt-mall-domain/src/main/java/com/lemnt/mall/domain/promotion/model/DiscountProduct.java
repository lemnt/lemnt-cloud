package com.lemnt.mall.domain.promotion.model;

import java.math.BigDecimal;

/** 活动下独立配置的 SKU 折扣。 */
public record DiscountProduct(
        Long id,
        Long activityId,
        Long spuId,
        Long skuId,
        String discountType,
        Integer discountPercent,
        BigDecimal discountPrice
) implements java.io.Serializable {
}
