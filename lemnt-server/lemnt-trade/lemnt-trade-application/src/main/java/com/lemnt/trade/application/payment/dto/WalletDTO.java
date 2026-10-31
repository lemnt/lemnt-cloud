package com.lemnt.trade.application.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 钱包账户应用输出。 */
public record WalletDTO(
        Long id,
        Long memberId,
        BigDecimal balance,
        BigDecimal freezePrice,
        BigDecimal totalExpense,
        BigDecimal totalRecharge,
        LocalDateTime createTime
) implements Serializable {
}
