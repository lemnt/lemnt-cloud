package com.lemnt.pay.application.payment.cmd;

import java.io.Serializable;

/**
 * 渠道支付通知接收命令，报文与验签材料由渠道回调头传入。
 */
public record PaymentNotifyReceiveCmd(
    String appKey,
    String channel,
    String body,
    String signature,
    String timestamp,
    String nonce,
    String serialNumber,
    String signType)
    implements Serializable {
}
