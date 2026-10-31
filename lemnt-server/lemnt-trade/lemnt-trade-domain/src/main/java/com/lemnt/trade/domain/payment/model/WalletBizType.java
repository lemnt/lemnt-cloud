package com.lemnt.trade.domain.payment.model;

/** 钱包流水业务类型。 */
public enum WalletBizType {
    RECHARGE("充值"),
    RECHARGE_REFUND("充值退款"),
    PAYMENT("支付"),
    PAYMENT_REFUND("支付退款"),
    UPDATE_BALANCE("调整余额"),
    TRANSFER("转账");

    private final String description;

    WalletBizType(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
