package com.lemnt.trade.domain.order.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 订单数量与应付金额汇总。 */
public record OrderTotal(
        long orderCount,
        BigDecimal orderPayPrice
) implements Serializable {
}
