package com.lemnt.third.sdk.wechatpay;

/**
 * 退款状态：微信的异常单需要人工核对，因此与关闭订单一并归为失败。
 */
public enum WechatRefundStatus {
    SUCCESS,
    PROCESSING,
    FAILED
}
