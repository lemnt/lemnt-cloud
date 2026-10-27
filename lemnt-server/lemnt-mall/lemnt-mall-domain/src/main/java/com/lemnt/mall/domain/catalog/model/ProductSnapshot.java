package com.lemnt.mall.domain.catalog.model;

import java.math.BigDecimal;

/**
 * 商品快照。
 */
public record ProductSnapshot(
        Long id,
        String productCode,
        String name,
        BigDecimal price,
        Integer stock
) {}
