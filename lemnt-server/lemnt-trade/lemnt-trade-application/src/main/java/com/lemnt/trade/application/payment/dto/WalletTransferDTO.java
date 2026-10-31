package com.lemnt.trade.application.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 钱包转账应用输出。 */
public record WalletTransferDTO(
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
) implements Serializable {
}
