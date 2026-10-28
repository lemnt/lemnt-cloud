package com.lemnt.mall.domain.settlement.model;

import com.lemnt.mall.domain.cart.model.CartReference;

import java.time.Instant;
import java.util.List;

/**
 * 结算。
 */
public record Settlement(
        String id,
        Long memberId,
        SettlementQuote quote,
        List<CartReference> references,
        Instant expiresAt
) {
    /**
     * 创建结算实例。
     */
    public Settlement {
        references = List.copyOf(references);
    }
}
