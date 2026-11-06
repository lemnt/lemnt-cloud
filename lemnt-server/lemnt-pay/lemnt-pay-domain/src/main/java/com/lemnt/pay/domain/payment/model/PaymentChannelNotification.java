package com.lemnt.pay.domain.payment.model;

/**
 * 渠道回调的报文与验签材料；各渠道按自身协议取用，缺失字段由适配器明确拒绝。
 */
public record PaymentChannelNotification(
    String body,
    String signature,
    String timestamp,
    String nonce,
    String serialNumber,
    String signType) {
}
