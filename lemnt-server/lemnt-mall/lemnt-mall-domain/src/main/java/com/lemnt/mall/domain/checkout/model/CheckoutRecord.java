package com.lemnt.mall.domain.checkout.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * 结算记录。
 */
public record CheckoutRecord(
        String orderNo,
        Long memberId,
        String requestNo,
        String fingerprint,
        BigDecimal originalAmount,
        BigDecimal discountAmount,
        BigDecimal payableAmount,
        Long couponId,
        List<CartReference> cartReferences
) {
    /**
     * 创建结算记录实例。
     */
    public CheckoutRecord {
        cartReferences = List.copyOf(cartReferences);
    }
}
