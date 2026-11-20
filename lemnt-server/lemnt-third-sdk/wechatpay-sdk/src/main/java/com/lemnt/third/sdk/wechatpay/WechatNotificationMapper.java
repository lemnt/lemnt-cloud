package com.lemnt.third.sdk.wechatpay;

import com.wechat.pay.java.service.payments.model.Transaction;

/**
 * 把微信交易对象映射为不暴露渠道类型的交易事实。
 */
final class WechatNotificationMapper {
    private WechatNotificationMapper() {
    }

    static WechatTransaction toTransaction(Transaction value) {
        if (value == null) {
            return null;
        }
        var amount = value.getAmount();
        return new WechatTransaction(
            value.getOutTradeNo(),
            value.getAttach(),
            value.getTransactionId(),
            value.getAppid(),
            value.getMchid(),
            value.getTradeState() == null ? null : value.getTradeState().name(),
            amount == null ? null : amount.getCurrency(),
            amount == null ? null : amount.getTotal());
    }
}
