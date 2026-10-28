package com.lemnt.mall.domain.settlement.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/** 商城运费及行分摊，金额元。 */
public record DeliveryPriceResult(
        BigDecimal total,
        Map<Long, BigDecimal> itemPrices
) implements Serializable {
}
