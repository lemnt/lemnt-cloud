package com.lemnt.trade.infrastructure.order.dataobject;

import java.io.Serializable;
import java.math.BigDecimal;

/** 订单汇总查询投影。 */
public record OrderTotalDO(
        long orderCount,
        BigDecimal orderPayPrice
) implements Serializable {
}
