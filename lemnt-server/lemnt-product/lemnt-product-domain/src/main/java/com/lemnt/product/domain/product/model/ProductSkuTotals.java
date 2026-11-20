package com.lemnt.product.domain.product.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 商品当前 SKU 的价格下限和可用库存合计，历史价格不全时保留空值。 */
public record ProductSkuTotals(
        Long spuId, BigDecimal price, BigDecimal marketPrice, BigDecimal costPrice, long stock)
        implements Serializable {}
