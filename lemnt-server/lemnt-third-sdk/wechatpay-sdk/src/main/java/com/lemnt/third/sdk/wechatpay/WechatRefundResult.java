package com.lemnt.third.sdk.wechatpay;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 微信退款结果：金额为元，状态已归并为成功、处理中、失败。
 */
public record WechatRefundResult(
    String outRefundNo,
    String refundId,
    String outTradeNo,
    BigDecimal refundAmount,
    WechatRefundStatus status)
    implements Serializable {
}
