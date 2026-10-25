package com.lemnt.trade.application.checkout.dto;

public record CheckoutQuoteDTO(
        java.util.List<CheckoutItemDTO> items,
        java.math.BigDecimal originalAmount,
        java.math.BigDecimal discountAmount,
        java.math.BigDecimal payableAmount,
        Long couponId,
        String quoteToken) {}
