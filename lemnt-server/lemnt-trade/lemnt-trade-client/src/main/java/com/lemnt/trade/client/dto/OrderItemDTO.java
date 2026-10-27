package com.lemnt.trade.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 交易订单明细。
 */
public record OrderItemDTO(
        Long productId,
        String productCode,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineAmount
) implements Serializable {}
