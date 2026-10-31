package com.lemnt.trade.domain.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 钱包充值单，金额为下单时套餐快照。 */
public record WalletRecharge(
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
) {
}
