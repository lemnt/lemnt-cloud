package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.third.sdk.wechatpay.WechatPayClient;
import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 微信支付关单：成功、已关闭与未知结果的处理。
 */
class WechatPaymentChannelCloseClientTest {
    final WechatPayClientProvider clients = mock(WechatPayClientProvider.class);
    final WechatPayClient client = mock(WechatPayClient.class);
    final NativePayService nativePay = mock(NativePayService.class);
    final WechatPaymentChannelCloseClient close = new WechatPaymentChannelCloseClient(clients);

    final PayChannelConfig config =
        new PayChannelConfig(1L, 1L, "WECHAT", 0, BigDecimal.ZERO, null, "{}", null);
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
        when(clients.get(config)).thenReturn(client);
        when(client.nativePay()).thenReturn(nativePay);
        when(client.mchId()).thenReturn("1900000001");
    }

    @Test
    void closeUnpaidClosesChannelOrder() {
        assertTrue(close.closeUnpaid(payment, config));

        ArgumentCaptor<CloseOrderRequest> captor = ArgumentCaptor.forClass(CloseOrderRequest.class);
        verify(nativePay).closeOrder(captor.capture());
        assertEquals("1900000001", captor.getValue().getMchid());
        assertEquals("PAY1", captor.getValue().getOutTradeNo());
    }

    @Test
    void closeUnpaidTreatsAlreadyClosedOrMissingOrderAsClosed() {
        doThrow(serviceError("ORDER_CLOSED")).when(nativePay).closeOrder(any());
        assertTrue(close.closeUnpaid(payment, config));

        doThrow(serviceError("ORDER_NOT_EXIST")).when(nativePay).closeOrder(any());
        assertTrue(close.closeUnpaid(payment, config));
    }

    @Test
    void closeUnpaidDoesNotClaimUnknownChannelResultIsClosed() {
        doThrow(serviceError("SYSTEM_ERROR")).when(nativePay).closeOrder(any());

        assertFalse(close.closeUnpaid(payment, config));
    }

    private ServiceException serviceError(String code) {
        return new ServiceException(null, 400, "{\"code\":\"" + code + "\",\"message\":\"x\"}");
    }
}
