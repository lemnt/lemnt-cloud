package com.lemnt.mall.domain.store.model;

import com.lemnt.mall.domain.checkout.model.CartLine;

/**
 * 购物车增量加购。
 */
public record CartAddition(
        Long memberId,
        String requestNo,
        Long productId,
        int quantity,
        CartLine result
) {}
