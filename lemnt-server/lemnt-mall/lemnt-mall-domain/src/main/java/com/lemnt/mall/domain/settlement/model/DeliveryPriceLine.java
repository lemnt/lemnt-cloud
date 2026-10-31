package com.lemnt.mall.domain.settlement.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 商城运费商品快照，金额元。 */
public record DeliveryPriceLine(
        Long skuId,
        Long templateId,
        Integer count,
        BigDecimal weight,
        BigDecimal volume,
        BigDecimal payAmount
) implements Serializable {
}
