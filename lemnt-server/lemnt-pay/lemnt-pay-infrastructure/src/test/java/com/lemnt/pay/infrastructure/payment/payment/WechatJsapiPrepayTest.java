package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.third.sdk.wechatpay.WechatPayClient;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 小程序 JSAPI 下单：openid、金额换算与调起支付参数。
 */
class WechatJsapiPrepayTest {
    final WechatPayClient client = mock(WechatPayClient.class);
    final JsapiServiceExtension jsapi = mock(JsapiServiceExtension.class);
    final PaymentRecord payment =
        new PaymentRecord(
            1L,
            "PAY1",
            "TR1",
            "WECHAT",
            "lemnt-mall",
            null,
            new BigDecimal("8.00"),
            "WAITING",
            null);

    @BeforeEach
    void setUp() {
        when(client.appId()).thenReturn("wx-app");
        when(client.mchId()).thenReturn("1900000001");
        when(client.payNotifyUrl()).thenReturn("https://mall.example.com/trade/payment/notify");
        when(client.jsapi()).thenReturn(jsapi);
    }

    @Test
    void createSubmitsOpenIdAndReturnsRequestPaymentParams() {
        when(jsapi.prepayWithRequestPayment(any())).thenReturn(response());

        var prepay = WechatJsapiPrepay.create(client, payment, "OPENID1");

        ArgumentCaptor<PrepayRequest> captor = ArgumentCaptor.forClass(PrepayRequest.class);
        verify(jsapi).prepayWithRequestPayment(captor.capture());
        PrepayRequest request = captor.getValue();
        assertEquals("OPENID1", request.getPayer().getOpenid());
        assertEquals("wx-app", request.getAppid());
        assertEquals("1900000001", request.getMchid());
        assertEquals("PAY1", request.getOutTradeNo());
        assertEquals("TR1", request.getAttach());
        assertEquals(800, request.getAmount().getTotal());
        assertEquals("CNY", request.getAmount().getCurrency());
        assertEquals("wx-app", prepay.params().get("appId"));
        assertEquals("1700000000", prepay.params().get("timeStamp"));
        assertEquals("prepay_id=wx1", prepay.params().get("package"));
        assertEquals("SIGN", prepay.params().get("paySign"));
        assertNull(prepay.payUrl());
    }

    @Test
    void createRejectsResponseWithoutPaymentSignature() {
        PrepayWithRequestPaymentResponse response = response();
        response.setPaySign(" ");
        when(jsapi.prepayWithRequestPayment(any())).thenReturn(response);

        assertThrows(
            ServiceException.class, () -> WechatJsapiPrepay.create(client, payment, "OPENID1"));
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
