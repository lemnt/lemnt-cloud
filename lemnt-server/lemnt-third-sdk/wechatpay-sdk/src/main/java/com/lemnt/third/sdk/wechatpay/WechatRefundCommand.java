package com.lemnt.third.sdk.wechatpay;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 微信退款入参：退款单号即渠道幂等键，金额为本次退款与原支付总额。
 */
public record WechatRefundCommand(
    String outRefundNo,
    String outTradeNo,
    BigDecimal refundAmount,
    BigDecimal totalAmount,
    String reason)
    implements Serializable {

    public WechatRefundCommand {
        WechatPrepaySupport.requireText(outRefundNo, 64, "商户退款单号");
        WechatPrepaySupport.requireText(outTradeNo, 32, "商户支付单号");
        WechatPrepaySupport.requireAmount(refundAmount, "退款金额");
        WechatPrepaySupport.requireAmount(totalAmount, "原支付金额");
        if (refundAmount.compareTo(totalAmount) > 0) {
            throw new IllegalArgumentException("退款金额不能超过原支付金额");
        }
    }
}
