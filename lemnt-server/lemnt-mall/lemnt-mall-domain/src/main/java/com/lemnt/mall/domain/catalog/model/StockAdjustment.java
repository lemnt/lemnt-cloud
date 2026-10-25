package com.lemnt.mall.domain.catalog.model;

public record StockAdjustment(
        String requestNo, Long productId, int delta, String reason, Integer resultingStock) {}
