package com.lemnt.mall.domain.settlement.model;

import com.lemnt.mall.domain.cart.model.CartReference;

import java.math.BigDecimal;
import java.util.List;

/**
 * 结算记录。
 */
public record SettlementRecord(
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
    public SettlementRecord {
        cartReferences = List.copyOf(cartReferences);
    }
}
