package com.lemnt.trade.domain.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 钱包间转账，金额单位元。 */
public record WalletTransfer(
        Long id,
        String transferNo,
        String requestNo,
        Long fromWalletId,
        Long fromMemberId,
        Long toWalletId,
        Long toMemberId,
        BigDecimal amount,
        String remark,
        String status,
        LocalDateTime createTime
) {
}
