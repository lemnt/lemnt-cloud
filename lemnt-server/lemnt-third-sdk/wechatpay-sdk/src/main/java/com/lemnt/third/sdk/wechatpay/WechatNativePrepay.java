package com.lemnt.third.sdk.wechatpay;

import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;

import java.util.Map;

/**
 * 微信 Native 下单：组装扫码支付参数并映射为二维码地址。
 */
final class WechatNativePrepay {
    private static final String PARAM_CODE_URL = "codeUrl";

    private WechatNativePrepay() {
    }

    static WechatPrepayResult create(
        NativePayService service, WechatPrepayCommand command, String appId, String mchId) {
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
        PrepayResponse response = service.prepay(request);
        if (response == null || response.getCodeUrl() == null || response.getCodeUrl().isBlank()) {
            throw new IllegalStateException("微信支付未返回支付二维码");
        }
        return new WechatPrepayResult(
            WechatPrepayType.NATIVE, Map.of(PARAM_CODE_URL, response.getCodeUrl()));
    }
}
