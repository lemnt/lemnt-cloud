package com.lemnt.trade.domain.checkout.model;

public record CheckoutQuote(
        java.util.List<com.lemnt.trade.domain.model.TradeOrderItem> items,
        java.math.BigDecimal originalAmount,
        java.math.BigDecimal discountAmount,
        java.math.BigDecimal payableAmount,
        Long couponId) {
    public CheckoutQuote {
        items = java.util.List.copyOf(items);
    }
}
