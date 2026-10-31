package com.lemnt.trade.domain.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 会员钱包账户，金额单位元。 */
public record Wallet(
        Long id,
        Long memberId,
        BigDecimal balance,
        BigDecimal freezePrice,
        BigDecimal totalExpense,
        BigDecimal totalRecharge,
        LocalDateTime createTime
) {
}
