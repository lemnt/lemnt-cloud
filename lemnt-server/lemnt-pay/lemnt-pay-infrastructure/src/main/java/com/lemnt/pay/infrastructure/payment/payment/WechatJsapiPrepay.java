package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.domain.payment.model.PaymentChannel;
import com.lemnt.pay.domain.payment.model.PaymentPrepay;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.third.sdk.wechatpay.WechatPayClient;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信小程序 JSAPI 下单：按会员已绑定的 openid 下单，返回 wx.requestPayment 所需参数。
 */
final class WechatJsapiPrepay {
    private static final String PARAM_PACKAGE = "package";

    private WechatJsapiPrepay() {
    }

    static PaymentPrepay create(WechatPayClient client, PaymentRecord payment, String openId) {
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
        Payer payer = new Payer();
        payer.setOpenid(openId);
        request.setPayer(payer);
        PrepayWithRequestPaymentResponse response =
            client.jsapi().prepayWithRequestPayment(request);
        Map<String, String> params = new LinkedHashMap<>();
        put(params, "appId", response == null ? null : response.getAppId());
        put(params, "timeStamp", response == null ? null : response.getTimeStamp());
        put(params, "nonceStr", response == null ? null : response.getNonceStr());
        put(params, PARAM_PACKAGE, response == null ? null : response.getPackageVal());
        put(params, "signType", response == null ? null : response.getSignType());
        put(params, "paySign", response == null ? null : response.getPaySign());
        return new PaymentPrepay(payment.paymentNo(), PaymentChannel.WECHAT.name(), null, params);
    }

    private static void put(Map<String, String> params, String key, String value) {
        if (value == null || value.isBlank()) {
            throw new ServiceException("微信支付未返回" + key, 400);
        }
        params.put(key, value);
    }
}
