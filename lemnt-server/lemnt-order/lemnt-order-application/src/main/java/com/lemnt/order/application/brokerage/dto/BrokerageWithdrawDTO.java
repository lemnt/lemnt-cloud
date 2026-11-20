package com.lemnt.order.application.brokerage.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提现单输出。
 */
public record BrokerageWithdrawDTO(
    Long id,
    Long userId,
    BigDecimal price,
    BigDecimal feePrice,
    BigDecimal totalPrice,
    Integer type,
    String userName,
    String userAccount,
    String qrCodeUrl,
    String bankName,
    String bankAddress,
    Integer status,
    String auditReason,
    LocalDateTime auditTime,
    LocalDateTime createTime)
    implements Serializable {
}
