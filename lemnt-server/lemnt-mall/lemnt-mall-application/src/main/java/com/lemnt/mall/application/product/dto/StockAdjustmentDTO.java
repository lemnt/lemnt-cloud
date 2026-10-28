package com.lemnt.mall.application.product.dto;

/**
 * 库存调整数据传输对象。
 */
public record StockAdjustmentDTO(
        String requestNo,
        Long productId,
        int delta,
        String reason,
        Integer resultingStock
) implements java.io.Serializable {}
