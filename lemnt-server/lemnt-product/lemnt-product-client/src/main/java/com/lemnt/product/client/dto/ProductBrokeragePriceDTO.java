package com.lemnt.product.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/** 在售 SKU 的标价与一级每件固定佣金，单位元。 */
public record ProductBrokeragePriceDTO(BigDecimal price, BigDecimal firstFixed)
        implements Serializable {}
