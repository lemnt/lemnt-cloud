package com.lemnt.mall.application.checkout.dto;

import java.math.BigDecimal;

/**
 * 结算明细数据传输对象。
 */
public record CheckoutItemDTO(
        Long productId,
        String productCode,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineAmount
) implements java.io.Serializable {}
