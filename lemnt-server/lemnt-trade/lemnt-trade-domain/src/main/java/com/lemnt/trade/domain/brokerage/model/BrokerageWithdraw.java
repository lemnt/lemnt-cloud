package com.lemnt.trade.domain.brokerage.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 佣金提现单，金额为元。 */
public record BrokerageWithdraw(
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
        String remark,
        LocalDateTime createTime
) implements Serializable {
}
