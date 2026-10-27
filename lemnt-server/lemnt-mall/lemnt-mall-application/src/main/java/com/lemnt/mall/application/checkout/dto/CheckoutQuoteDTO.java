package com.lemnt.mall.application.checkout.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 结算报价数据传输对象。
 */
public record CheckoutQuoteDTO(
        List<CheckoutItemDTO> items,
        BigDecimal originalAmount,
        BigDecimal discountAmount,
        BigDecimal payableAmount,
        Long couponId,
        String quoteToken
) implements java.io.Serializable {}
