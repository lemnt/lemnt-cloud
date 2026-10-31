package com.lemnt.trade.domain.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 钱包余额流水，amount 正增负减，balanceAfter 为交易后可用余额。 */
public record WalletTransaction(
        Long id,
        String transactionNo,
        Long walletId,
        String bizType,
        String bizNo,
        String title,
        BigDecimal amount,
        BigDecimal balanceAfter,
        LocalDateTime createTime
) {
}
