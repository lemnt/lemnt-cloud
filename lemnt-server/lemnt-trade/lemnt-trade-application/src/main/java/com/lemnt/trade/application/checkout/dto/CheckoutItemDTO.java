package com.lemnt.trade.application.checkout.dto;

public record CheckoutItemDTO(
        Long productId,
        String productCode,
        String productName,
        java.math.BigDecimal unitPrice,
        Integer quantity,
        java.math.BigDecimal lineAmount) {}
