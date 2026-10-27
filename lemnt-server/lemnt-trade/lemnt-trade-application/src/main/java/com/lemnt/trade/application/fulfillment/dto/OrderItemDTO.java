package com.lemnt.trade.application.fulfillment.dto;

import java.math.BigDecimal;

/**
 * 结算明细数据传输对象。
 */
public record OrderItemDTO(
        Long productId,
        String productCode,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineAmount
) implements java.io.Serializable {}
