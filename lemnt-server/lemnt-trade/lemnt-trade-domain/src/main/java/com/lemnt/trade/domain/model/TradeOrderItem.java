package com.lemnt.trade.domain.model;

import java.math.BigDecimal;

public record TradeOrderItem(Long productId, String productCode, String productName,
                             BigDecimal unitPrice, Integer quantity, BigDecimal lineAmount) { }
