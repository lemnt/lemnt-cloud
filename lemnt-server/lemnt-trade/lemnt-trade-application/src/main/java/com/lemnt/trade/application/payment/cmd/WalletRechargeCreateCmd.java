package com.lemnt.trade.application.payment.cmd;

import java.io.Serializable;

/** 按套餐创建充值单。 */
public record WalletRechargeCreateCmd(
        Long packageId,
        String requestNo
) implements Serializable {
}
