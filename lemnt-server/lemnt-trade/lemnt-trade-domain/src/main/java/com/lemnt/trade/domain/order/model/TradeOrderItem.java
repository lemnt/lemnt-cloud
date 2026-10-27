package com.lemnt.trade.domain.order.model;

import java.math.BigDecimal;

/**
 * 交易订单明细。
 */
public record TradeOrderItem(
        Long productId,
        String productCode,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineAmount
) {}
