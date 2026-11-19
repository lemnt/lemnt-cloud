package com.lemnt.product.domain.product.model;

import java.math.BigDecimal;

/** SKU当前每件固定佣金，单位元。 */
public record ProductFixedCommission(Long skuId, BigDecimal first, BigDecimal second) {}
