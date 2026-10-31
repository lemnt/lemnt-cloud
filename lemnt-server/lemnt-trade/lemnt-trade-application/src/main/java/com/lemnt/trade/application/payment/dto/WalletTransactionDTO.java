package com.lemnt.trade.application.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 钱包流水应用输出。 */
public record WalletTransactionDTO(
        String transactionNo,
        String bizType,
        String bizNo,
        String title,
        BigDecimal amount,
        BigDecimal balanceAfter,
        LocalDateTime createTime
) implements Serializable {
}
