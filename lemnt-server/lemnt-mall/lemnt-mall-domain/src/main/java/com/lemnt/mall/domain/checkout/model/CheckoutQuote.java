package com.lemnt.mall.domain.checkout.model;

import com.lemnt.mall.domain.checkout.model.CheckoutItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * 结算报价。
 */
public record CheckoutQuote(
        List<CheckoutItem> items,
        BigDecimal originalAmount,
        BigDecimal discountAmount,
        BigDecimal payableAmount,
        Long couponId
) {
    /**
     * 创建结算报价实例。
     */
    public CheckoutQuote {
        items = List.copyOf(items);
    }
}
