package com.lemnt.third.sdk.wechatpay;

import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信小程序 JSAPI 下单：组装付款人 openid 并返回 wx.requestPayment 参数。
 */
final class WechatJsapiPrepay {
    private static final String PARAM_PACKAGE = "package";

    private WechatJsapiPrepay() {
    }

    static WechatPrepayResult create(
        JsapiServiceExtension service,
        WechatPrepayCommand command,
        String appId,
        String mchId) {
        PrepayRequest request = new PrepayRequest();
        request.setAppid(appId);
        request.setMchid(mchId);
        request.setDescription(WechatPrepaySupport.description(command.orderNo()));
        request.setOutTradeNo(command.outTradeNo());
        request.setAttach(command.orderNo());
        request.setNotifyUrl(command.notifyUrl());
        Amount amount = new Amount();
        amount.setCurrency(WechatPrepaySupport.CURRENCY);
        amount.setTotal(WechatPrepaySupport.fenInt(command.amount(), "支付金额"));
        request.setAmount(amount);
        Payer payer = new Payer();
        payer.setOpenid(command.openId());
        request.setPayer(payer);
        PrepayWithRequestPaymentResponse response = service.prepayWithRequestPayment(request);
        Map<String, String> params = new LinkedHashMap<>();
        put(params, "appId", response == null ? null : response.getAppId());
        put(params, "timeStamp", response == null ? null : response.getTimeStamp());
        put(params, "nonceStr", response == null ? null : response.getNonceStr());
        put(params, PARAM_PACKAGE, response == null ? null : response.getPackageVal());
        put(params, "signType", response == null ? null : response.getSignType());
        put(params, "paySign", response == null ? null : response.getPaySign());
        return new WechatPrepayResult(WechatPrepayType.JSAPI, params);
    }

    private static void put(Map<String, String> params, String key, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("微信支付未返回" + key);
        }
        params.put(key, value);
    }
}
