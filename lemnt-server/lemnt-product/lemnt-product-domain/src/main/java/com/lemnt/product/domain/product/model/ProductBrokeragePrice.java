package com.lemnt.product.domain.product.model;

import java.math.BigDecimal;

/** 商品预估读取的 SKU 价格，固定佣金为空时使用全局比例。 */
public record ProductBrokeragePrice(BigDecimal price, BigDecimal firstFixed) {}
