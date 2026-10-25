package com.lemnt.mall.application.catalog.dto;

public record StockAdjustmentDTO(
        String requestNo, Long productId, int delta, String reason, Integer resultingStock) {}
