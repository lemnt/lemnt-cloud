package com.lemnt.third.sdk.wechatpay;

import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 扫码下单：业务入参映射为微信请求，返回二维码地址。
 */
class WechatNativePrepayTest {
    final NativePayService service = mock(NativePayService.class);
    final WechatPrepayCommand command =
        new WechatPrepayCommand(
            "PAY1", "TR1", new BigDecimal("8.00"), "https://mall.example.com/notify", null);

    @Test
    void createMapsBusinessFieldsToNativeRequest() {
        PrepayResponse response = new PrepayResponse();
        response.setCodeUrl("weixin://wxpay/bizpayurl?pr=abc");
        when(service.prepay(any())).thenReturn(response);

        var result = WechatNativePrepay.create(service, command, "wx-app", "1900000001");

        ArgumentCaptor<PrepayRequest> captor = ArgumentCaptor.forClass(PrepayRequest.class);
        verify(service).prepay(captor.capture());
        PrepayRequest request = captor.getValue();
        assertEquals("wx-app", request.getAppid());
        assertEquals("1900000001", request.getMchid());
        assertEquals("PAY1", request.getOutTradeNo());
        assertEquals("TR1", request.getAttach());
        assertEquals("https://mall.example.com/notify", request.getNotifyUrl());
        assertEquals(800, request.getAmount().getTotal());
        assertEquals("CNY", request.getAmount().getCurrency());
        assertTrue(request.getDescription().contains("TR1"));
        assertEquals(WechatPrepayType.NATIVE, result.type());
        assertEquals("weixin://wxpay/bizpayurl?pr=abc", result.params().get("codeUrl"));
    }

    @Test
    void createRejectsMissingCodeUrl() {
        when(service.prepay(any())).thenReturn(new PrepayResponse());

        assertThrows(
            IllegalStateException.class,
            () -> WechatNativePrepay.create(service, command, "wx-app", "1900000001"));
    }
}
