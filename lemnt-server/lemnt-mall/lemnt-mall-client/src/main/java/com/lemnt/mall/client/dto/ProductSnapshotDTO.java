package com.lemnt.mall.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品快照数据传输对象。
 */
public record ProductSnapshotDTO(
        Long id,
        String productCode,
        String name,
        BigDecimal price,
        Integer stock
) implements Serializable {}
