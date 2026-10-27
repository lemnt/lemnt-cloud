package com.lemnt.mall.domain.catalog.model;

/**
 * 库存调整。
 */
public record StockAdjustment(
        String requestNo,
        Long productId,
        int delta,
        String reason,
        Integer resultingStock
) {}
