package com.lemnt.trade.application.payment.cmd;

import java.io.Serializable;
import java.math.BigDecimal;

/** 会员钱包转账。 */
public record WalletTransferCreateCmd(
        Long toMemberId,
        BigDecimal amount,
        String remark,
        String requestNo
) implements Serializable {
}
