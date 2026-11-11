package com.lemnt.product.application.product.dto;

import java.io.Serializable;

/**
 * 库存调整数据传输对象。
 */
public record ProductStockAdjustmentDTO(
    String requestNo, Long productId, int delta, String reason, Integer resultingStock)
    implements Serializable {
}
