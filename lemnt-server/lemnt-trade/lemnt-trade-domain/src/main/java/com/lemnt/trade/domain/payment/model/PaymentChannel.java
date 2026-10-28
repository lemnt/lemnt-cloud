package com.lemnt.trade.domain.payment.model;

/** 系统支持的支付渠道；具体渠道接入状态由渠道适配器决定。 */
public enum PaymentChannel {
    ALIPAY("支付宝"),
    WECHAT("微信支付");

    private final String displayName;

    PaymentChannel(String displayName) {
        this.displayName = displayName;
    }

    /** 获取渠道展示名称。 */
    public String displayName() {
        return displayName;
    }
}
