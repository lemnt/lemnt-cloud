package com.lemnt.third.sdk.wechatpay;

import com.wechat.pay.java.core.exception.ServiceException;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.CloseOrderRequest;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 关单：只有渠道确认关闭或订单不存在才算成功。
 */
class WechatCloseSupportTest {
    final NativePayService service = mock(NativePayService.class);

    @Test
    void closeSendsMerchantAndPaymentNo() {
        assertTrue(WechatCloseSupport.close(service, "PAY1", "1900000001"));

        ArgumentCaptor<CloseOrderRequest> captor =
            ArgumentCaptor.forClass(CloseOrderRequest.class);
        verify(service).closeOrder(captor.capture());
        assertEquals("1900000001", captor.getValue().getMchid());
        assertEquals("PAY1", captor.getValue().getOutTradeNo());
    }

    @Test
    void closeTreatsAlreadyClosedOrMissingOrderAsClosed() {
        doThrow(serviceError("ORDER_CLOSED")).when(service).closeOrder(any());
        assertTrue(WechatCloseSupport.close(service, "PAY1", "1900000001"));

        doThrow(serviceError("ORDER_NOT_EXIST")).when(service).closeOrder(any());
        assertTrue(WechatCloseSupport.close(service, "PAY1", "1900000001"));
    }

    @Test
    void closeDoesNotClaimUnknownChannelResultIsClosed() {
        doThrow(serviceError("SYSTEM_ERROR")).when(service).closeOrder(any());

        assertFalse(WechatCloseSupport.close(service, "PAY1", "1900000001"));
    }

    private ServiceException serviceError(String code) {
        return new ServiceException(null, 400, "{\"code\":\"" + code + "\",\"message\":\"x\"}");
    }
}
