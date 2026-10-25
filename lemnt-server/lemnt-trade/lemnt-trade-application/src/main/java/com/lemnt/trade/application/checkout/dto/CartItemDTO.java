package com.lemnt.trade.application.checkout.dto;

public record CartItemDTO(
        Long id,
        Long productId,
        int quantity,
        boolean selected,
        long version,
        String name,
        String productCode,
        java.math.BigDecimal unitPrice,
        Integer availableStock,
        boolean sellable) {}
