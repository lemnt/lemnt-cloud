package com.lemnt.pay.domain.payment.model;

/**
 * 支付结果派发类型，内部通知与应用 HTTP 通知分别重试。
 */
public enum PayNotifyType {
    PAY,
    AFTER_SALE,
    REFUND,
    PAY_HTTP,
    REFUND_HTTP,
    REFUND_FAIL_HTTP
}
