package com.lemnt.mall.domain.promotion.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 砍价活动，商品为单一 SKU。 */
public record BargainActivity(
        Long id,
        String name,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer status,
        Long spuId,
        Long skuId,
        BigDecimal bargainFirstPrice,
        BigDecimal bargainMinPrice,
        Integer stock,
        Integer totalStock,
        Integer helpMaxCount,
        Integer bargainCount,
        Integer totalLimitCount,
        BigDecimal randomMinPrice,
        BigDecimal randomMaxPrice
) implements java.io.Serializable {
}
