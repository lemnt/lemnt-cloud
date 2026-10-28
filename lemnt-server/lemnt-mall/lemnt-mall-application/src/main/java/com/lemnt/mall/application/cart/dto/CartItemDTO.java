package com.lemnt.mall.application.cart.dto;

import java.math.BigDecimal;

/**
 * 购物车明细数据传输对象。
 */
public record CartItemDTO(
        Long id,
        Long productId,
        int quantity,
        boolean selected,
        long version,
        String name,
        String productCode,
        BigDecimal unitPrice,
        Integer availableStock,
        boolean sellable
) implements java.io.Serializable {}
