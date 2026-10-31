package com.lemnt.trade.application.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 充值单应用输出。 */
public record WalletRechargeDTO(
        Long id,
        String rechargeNo,
        String requestNo,
        Long walletId,
        Long memberId,
        Long packageId,
        BigDecimal payPrice,
        BigDecimal bonusPrice,
        BigDecimal totalPrice,
        String status,
        String channelCode,
        LocalDateTime paidTime,
        LocalDateTime refundedTime,
        LocalDateTime createTime
) implements Serializable {
}
