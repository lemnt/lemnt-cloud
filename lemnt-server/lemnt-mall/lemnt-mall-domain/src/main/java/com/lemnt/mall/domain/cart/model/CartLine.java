package com.lemnt.mall.domain.cart.model;

/**
 * 购物车明细。
 */
public record CartLine(
        Long id,
        Long memberId,
        Long productId,
        int quantity,
        boolean selected,
        long version
) {}
