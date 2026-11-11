package com.lemnt.third.sdk.wechatpay;

import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 小程序下单：组装付款人 openid 并返回 wx.requestPayment 参数。
 */
class WechatJsapiPrepayTest {
    final JsapiServiceExtension service = mock(JsapiServiceExtension.class);
    final WechatPrepayCommand command =
        new WechatPrepayCommand(
            "PAY1", "TR1", new BigDecimal("8.00"), "https://mall.example.com/notify", "OPENID1");

    @Test
    void createSubmitsOpenIdAndReturnsRequestPaymentParams() {
        when(service.prepayWithRequestPayment(any())).thenReturn(response());

        var result = WechatJsapiPrepay.create(service, command, "wx-app", "1900000001");

        ArgumentCaptor<PrepayRequest> captor = ArgumentCaptor.forClass(PrepayRequest.class);
        verify(service).prepayWithRequestPayment(captor.capture());
        PrepayRequest request = captor.getValue();
        assertEquals("OPENID1", request.getPayer().getOpenid());
        assertEquals("wx-app", request.getAppid());
        assertEquals("1900000001", request.getMchid());
        assertEquals("PAY1", request.getOutTradeNo());
        assertEquals("TR1", request.getAttach());
        assertEquals(800, request.getAmount().getTotal());
        assertEquals("CNY", request.getAmount().getCurrency());
        assertEquals(WechatPrepayType.JSAPI, result.type());
        assertEquals("wx-app", result.params().get("appId"));
        assertEquals("1700000000", result.params().get("timeStamp"));
        assertEquals("prepay_id=wx1", result.params().get("package"));
        assertEquals("SIGN", result.params().get("paySign"));
    }

    @Test
    void createRejectsResponseWithoutPaymentSignature() {
        PrepayWithRequestPaymentResponse response = response();
        response.setPaySign(" ");
        when(service.prepayWithRequestPayment(any())).thenReturn(response);

        assertThrows(
            IllegalStateException.class,
            () -> WechatJsapiPrepay.create(service, command, "wx-app", "1900000001"));
    }

    private PrepayWithRequestPaymentResponse response() {
        PrepayWithRequestPaymentResponse response = new PrepayWithRequestPaymentResponse();
        response.setAppId("wx-app");
        response.setTimeStamp("1700000000");
        response.setNonceStr("NONCE");
        response.setPackageVal("prepay_id=wx1");
        response.setSignType("RSA");
        response.setPaySign("SIGN");
        return response;
    }
}
