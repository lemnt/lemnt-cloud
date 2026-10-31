package com.lemnt.trade.application.order.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/** 订单管理统计。 */
public record OrderStatisticsDTO(
        long orderCount,
        BigDecimal orderPayPrice,
        long afterSaleCount,
        BigDecimal afterSalePrice
) implements Serializable {
}
