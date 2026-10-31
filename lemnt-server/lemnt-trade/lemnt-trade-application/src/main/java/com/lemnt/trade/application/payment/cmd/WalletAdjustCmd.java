package com.lemnt.trade.application.payment.cmd;

import java.io.Serializable;
import java.math.BigDecimal;

/** 后台人工调整钱包余额，amount 正增负减。 */
public record WalletAdjustCmd(
        String requestNo,
        BigDecimal amount,
        String reason
) implements Serializable {
}
