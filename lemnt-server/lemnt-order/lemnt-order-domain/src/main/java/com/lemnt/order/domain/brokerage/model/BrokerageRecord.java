package com.lemnt.order.domain.brokerage.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 佣金记录：ORDER 获得佣金、WITHDRAW 提现扣减、WITHDRAW_REJECT 驳回返还。
 */
public record BrokerageRecord(
    Long id,
    Long userId,
    String bizId,
    Integer bizType,
    String title,
    String description,
    BigDecimal price,
    BigDecimal totalPrice,
    Integer status,
    Integer frozenDays,
    LocalDateTime unfreezeTime,
    Integer sourceUserLevel,
    Long sourceUserId,
    LocalDateTime createTime)
    implements Serializable {
}
