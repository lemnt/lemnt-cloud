package com.lemnt.third.sdk.wechatpay;

import java.io.Serializable;

/**
 * 微信支付交易事实：字段与微信返回一一对应，是否可完成支付由调用方按业务判定。
 */
public record WechatTransaction(
    String outTradeNo,
    String orderNo,
    String transactionId,
    String appId,
    String mchId,
    String tradeState,
    String currency,
    Integer totalFen)
    implements Serializable {
}
