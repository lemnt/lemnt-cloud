package com.lemnt.product.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/** 订单建单读取的 SKU 每件固定佣金，一级和二级独立，单位元。 */
public record ProductFixedCommissionDTO(Long skuId, BigDecimal first, BigDecimal second)
        implements Serializable {}
