package com.lemnt.trade.domain.payment.model;

/** 充值单状态：待支付、已到账、已退款。 */
public enum WalletRechargeStatus {
    PENDING,
    PAID,
    REFUNDED
}
