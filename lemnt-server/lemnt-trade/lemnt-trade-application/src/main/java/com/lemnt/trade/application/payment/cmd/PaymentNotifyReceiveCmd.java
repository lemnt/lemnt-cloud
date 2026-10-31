package com.lemnt.trade.application.payment.cmd;

import java.io.Serializable;

/** 渠道支付通知接收命令，签名与时间戳由渠道回调头传入。 */
public record PaymentNotifyReceiveCmd(
        String appKey,
        String channel,
        String body,
        String signature,
        String timestamp
) implements Serializable {
}
