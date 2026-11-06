package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.domain.payment.model.PaymentChannel;
import com.lemnt.pay.domain.payment.model.PaymentPrepay;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.third.sdk.wechatpay.WechatPayClient;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;

import java.util.Map;

/**
 * 微信 Native 下单：返回支付二维码地址，供扫码或后台收银场景使用。
 */
final class WechatNativePrepay {
    private static final String PARAM_CODE_URL = "codeUrl";

    private WechatNativePrepay() {
    }

    static PaymentPrepay create(WechatPayClient client, PaymentRecord payment) {
        PrepayRequest request = new PrepayRequest();
        request.setAppid(client.appId());
        request.setMchid(client.mchId());
        request.setDescription(WechatPaymentSupport.description(payment.orderNo()));
        request.setOutTradeNo(payment.paymentNo());
        request.setAttach(payment.orderNo());
        request.setNotifyUrl(client.payNotifyUrl());
        Amount amount = new Amount();
        amount.setCurrency(WechatPaymentSupport.CURRENCY);
        amount.setTotal(WechatPaymentSupport.cents(payment.amount()));
        request.setAmount(amount);
        PrepayResponse response = client.nativePay().prepay(request);
        if (response == null || response.getCodeUrl() == null || response.getCodeUrl().isBlank()) {
            throw new ServiceException("微信支付未返回支付二维码", 400);
        }
        return new PaymentPrepay(
            payment.paymentNo(),
            PaymentChannel.WECHAT.name(),
            null,
            Map.of(PARAM_CODE_URL, response.getCodeUrl()));
    }
}
