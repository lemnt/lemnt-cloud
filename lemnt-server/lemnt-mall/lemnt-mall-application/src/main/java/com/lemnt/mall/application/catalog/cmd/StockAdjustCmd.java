package com.lemnt.mall.application.catalog.cmd;

/**
 * 库存调整命令。
 */
public record StockAdjustCmd(
        Long productId,
        String requestNo,
        int delta,
        String reason
) implements java.io.Serializable {}
