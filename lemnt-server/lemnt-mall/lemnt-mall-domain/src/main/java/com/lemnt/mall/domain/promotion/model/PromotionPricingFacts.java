package com.lemnt.mall.domain.promotion.model;

/**
 * 目录计价事实。
 */
public record PromotionPricingFacts(
        long skuId,
        long spuId,
        String productCode,
        String name,
        long unitPriceFen,
        int availableStock,
        String factsHash
) {}
