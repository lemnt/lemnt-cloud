package com.lemnt.mall.application.checkout.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 结算回执数据传输对象。
 */
public record CheckoutReceiptDTO(
        String orderNo,
        Long memberId,
        String status,
        BigDecimal originalAmount,
        BigDecimal discountAmount,
        BigDecimal payableAmount,
        Long couponId,
        List<CheckoutItemDTO> items
) implements java.io.Serializable {}
