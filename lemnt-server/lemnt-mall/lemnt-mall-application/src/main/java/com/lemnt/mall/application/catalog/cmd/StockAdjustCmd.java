package com.lemnt.mall.application.catalog.cmd;

public record StockAdjustCmd(Long productId, String requestNo, int delta, String reason) {}
