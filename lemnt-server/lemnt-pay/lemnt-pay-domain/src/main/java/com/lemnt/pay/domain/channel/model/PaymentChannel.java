package com.lemnt.pay.domain.channel.model;

/**
 * 系统支持的支付渠道；具体渠道接入状态由渠道适配器决定。
 */
public enum PaymentChannel {
    ALIPAY("支付宝"),
    WECHAT("微信支付"),
    WALLET("钱包余额"),
    /**
     * 仅用于联调与自动化验收，生产环境不创建该渠道配置且适配器默认关闭。
     */
    SANDBOX("沙箱");

    private final String displayName;

    /** 商城正式收银台支持的渠道，沙箱由联调环境单独装配。 */
    public boolean storefront() {
        return this == WECHAT || this == WALLET;
    }

    PaymentChannel(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取渠道展示名称。
     */
    public String displayName() {
        return displayName;
    }
}
