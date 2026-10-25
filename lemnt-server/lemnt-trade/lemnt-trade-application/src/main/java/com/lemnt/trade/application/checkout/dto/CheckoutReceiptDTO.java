package com.lemnt.trade.application.checkout.dto;

public record CheckoutReceiptDTO(
        String orderNo,
        Long memberId,
        String status,
        java.math.BigDecimal originalAmount,
        java.math.BigDecimal discountAmount,
        java.math.BigDecimal payableAmount,
        Long couponId,
        java.util.List<CheckoutItemDTO> items) {}
