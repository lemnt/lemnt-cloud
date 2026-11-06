package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentChannelNotification;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.third.sdk.wechatpay.WechatPayClient;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.model.TransactionAmount;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 微信支付渠道的预支付映射与回调核验。
 */
class WechatPaymentChannelAdapterTest {
    final WechatPayClientProvider clients = mock(WechatPayClientProvider.class);
    final WechatPayClient client = mock(WechatPayClient.class);
    final NativePayService nativePay = mock(NativePayService.class);
    final WechatPaymentChannelAdapter adapter = new WechatPaymentChannelAdapter(clients);

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
        when(client.appId()).thenReturn("wx-app");
        when(client.mchId()).thenReturn("1900000001");
        when(client.payNotifyUrl()).thenReturn("https://mall.example.com/trade/payment/notify");
    }

    @Test
    void prepayMapsAmountToCentsAndReturnsCodeUrl() {
        PrepayResponse response = new PrepayResponse();
        response.setCodeUrl("weixin://wxpay/bizpayurl?pr=abc");
        when(nativePay.prepay(any())).thenReturn(response);

        var prepay = adapter.prepay(payment, config, null);

        ArgumentCaptor<PrepayRequest> captor = ArgumentCaptor.forClass(PrepayRequest.class);
        verify(nativePay).prepay(captor.capture());
        PrepayRequest request = captor.getValue();
        assertEquals("wx-app", request.getAppid());
        assertEquals("1900000001", request.getMchid());
        assertEquals("PAY1", request.getOutTradeNo());
        assertEquals("TR1", request.getAttach());
        assertEquals("https://mall.example.com/trade/payment/notify", request.getNotifyUrl());
        assertEquals(800, request.getAmount().getTotal());
        assertEquals("CNY", request.getAmount().getCurrency());
        assertEquals("PAY1", prepay.paymentNo());
        assertEquals("WECHAT", prepay.channel());
        assertNull(prepay.payUrl());
        assertEquals("weixin://wxpay/bizpayurl?pr=abc", prepay.params().get("codeUrl"));
    }

    @Test
    void prepayRejectsAmountWithMoreThanTwoDecimals() {
        PaymentRecord invalid =
            new PaymentRecord(
                1L,
                "PAY1",
                "TR1",
                "WECHAT",
                "lemnt-mall",
                null,
                new BigDecimal("8.001"),
                "WAITING",
                null);

        assertThrows(ServiceException.class, () -> adapter.prepay(invalid, config, null));
        verify(nativePay, never()).prepay(any());
    }

    @Test
    void prepayWithPayerOpenIdUsesJsapiInsteadOfNativeQr() {
        JsapiServiceExtension jsapi = mock(JsapiServiceExtension.class);
        when(client.jsapi()).thenReturn(jsapi);
        PrepayWithRequestPaymentResponse response = new PrepayWithRequestPaymentResponse();
        response.setAppId("wx-app");
        response.setTimeStamp("1700000000");
        response.setNonceStr("NONCE");
        response.setPackageVal("prepay_id=wx1");
        response.setSignType("RSA");
        response.setPaySign("SIGN");
        when(jsapi.prepayWithRequestPayment(any())).thenReturn(response);

        var prepay = adapter.prepay(payment, config, "OPENID1");

        assertEquals("wx-app", prepay.params().get("appId"));
        assertEquals("prepay_id=wx1", prepay.params().get("package"));
        assertEquals("SIGN", prepay.params().get("paySign"));
        assertNull(prepay.payUrl());
        verify(nativePay, never()).prepay(any());
    }

    @Test
    void prepayFailsWhenNotifyUrlMissing() {        when(client.payNotifyUrl()).thenReturn(" ");

        assertThrows(ServiceException.class, () -> adapter.prepay(payment, config, null));
        verify(nativePay, never()).prepay(any());
    }

    @Test
    void verifyMapsSuccessNotificationToYuanAmount() {
        when(client.parseTransactionNotification(any())).thenReturn(transaction());

        var verified = adapter.verify(config, notification());

        assertEquals("PAY1", verified.paymentNo());
        assertEquals("TR1", verified.orderNo());
        assertEquals("WECHAT", verified.channel());
        assertEquals("WX-1", verified.channelTradeNo());
        assertEquals(new BigDecimal("8.00"), verified.amount());
        assertEquals("WX-1", verified.notifyId());
    }

    @Test
    void verifyRejectsMismatchedMerchantOrApplication() {
        Transaction mismatchedMerchant = transaction();
        mismatchedMerchant.setMchid("1900000002");
        when(client.parseTransactionNotification(any())).thenReturn(mismatchedMerchant);
        assertThrows(ServiceException.class, () -> adapter.verify(config, notification()));

        Transaction mismatchedApp = transaction();
        mismatchedApp.setAppid("wx-other");
        when(client.parseTransactionNotification(any())).thenReturn(mismatchedApp);
        assertThrows(ServiceException.class, () -> adapter.verify(config, notification()));
    }

    @Test
    void verifyRejectsNotificationWithoutSuccessfulTradeState() {
        Transaction notPaid = transaction();
        notPaid.setTradeState(Transaction.TradeStateEnum.NOTPAY);
        when(client.parseTransactionNotification(any())).thenReturn(notPaid);

        assertThrows(ServiceException.class, () -> adapter.verify(config, notification()));
    }

    @Test
    void verifyRejectsNotificationWithoutOrderNumber() {
        Transaction withoutAttach = transaction();
        withoutAttach.setAttach(null);
        when(client.parseTransactionNotification(any())).thenReturn(withoutAttach);

        assertThrows(ServiceException.class, () -> adapter.verify(config, notification()));
    }

    private PaymentChannelNotification notification() {
        return new PaymentChannelNotification(
            "{}", "SIGN", "1700000000", "NONCE", "SERIAL", "WECHATPAY2-SHA256-RSA2048");
    }

    private Transaction transaction() {
        Transaction transaction = new Transaction();
        transaction.setOutTradeNo("PAY1");
        transaction.setAttach("TR1");
        transaction.setTransactionId("WX-1");
        transaction.setAppid("wx-app");
        transaction.setMchid("1900000001");
        transaction.setTradeState(Transaction.TradeStateEnum.SUCCESS);
        TransactionAmount amount = new TransactionAmount();
        amount.setTotal(800);
        amount.setCurrency("CNY");
        transaction.setAmount(amount);
        return transaction;
    }
}
