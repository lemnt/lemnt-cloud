package com.lemnt.trade.domain.checkout.model;

public record CheckoutRecord(
        String orderNo,
        Long memberId,
        String requestNo,
        String fingerprint,
        java.math.BigDecimal originalAmount,
        java.math.BigDecimal discountAmount,
        java.math.BigDecimal payableAmount,
        Long couponId,
        java.util.List<CartReference> cartReferences) {
    public CheckoutRecord {
        cartReferences = java.util.List.copyOf(cartReferences);
    }
}
