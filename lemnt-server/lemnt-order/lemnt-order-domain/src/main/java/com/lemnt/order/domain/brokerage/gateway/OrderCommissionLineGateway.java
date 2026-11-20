package com.lemnt.order.domain.brokerage.gateway;

import com.lemnt.order.domain.brokerage.model.OrderCommissionLine;

import java.math.BigDecimal;
import java.util.List;

/** 建单和支付事务内持久化佣金行。 */
public interface OrderCommissionLineGateway {
    void insert(OrderCommissionLine line);

    List<OrderCommissionLine> lines(Long orderId);

    /** 按行一次写入已发金额，重复入账不得覆盖首次事实。 */
    boolean recordEarned(Long orderItemId, int level, BigDecimal earned);
}
