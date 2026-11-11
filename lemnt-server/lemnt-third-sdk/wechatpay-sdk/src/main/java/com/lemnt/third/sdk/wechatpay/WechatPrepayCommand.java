package com.lemnt.third.sdk.wechatpay;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 微信预支付入参：调用方只提供本地单号、订单号、金额与付款人标识。
 * openId 为空时使用扫码支付，非空时使用小程序 JSAPI；通知地址取自调用方配置。
 */
public record WechatPrepayCommand(
    String outTradeNo,
    String orderNo,
    BigDecimal amount,
    String notifyUrl,
    String openId)
    implements Serializable {

    public WechatPrepayCommand {
        WechatPrepaySupport.requireText(outTradeNo, 32, "商户支付单号");
        WechatPrepaySupport.requireText(orderNo, 128, "订单编号");
        WechatPrepaySupport.requireText(notifyUrl, 256, "支付通知地址");
        WechatPrepaySupport.requireAmount(amount, "支付金额");
        if (openId != null && openId.length() > 128) {
            throw new IllegalArgumentException("付款人标识长度不能超过128");
        }
    }
}
