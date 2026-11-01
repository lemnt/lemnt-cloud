package com.lemnt.trade.domain.brokerage.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 分销用户：保存推广关系、资格和可用/冻结佣金。 */
public record BrokerageUser(
        Long id,
        Long bindUserId,
        LocalDateTime bindUserTime,
        Boolean brokerageEnabled,
        LocalDateTime brokerageTime,
        BigDecimal brokeragePrice,
        BigDecimal frozenPrice
) implements Serializable {
}
