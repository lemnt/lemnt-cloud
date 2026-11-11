package com.lemnt.pay.infrastructure.channel.adapter.wechatpay;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.domain.channel.model.PayChannelConfig;
import com.lemnt.pay.domain.channel.model.PaymentChannelNotification;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.third.sdk.wechatpay.WechatPayClient;
import com.lemnt.third.sdk.wechatpay.WechatPrepayCommand;
import com.lemnt.third.sdk.wechatpay.WechatPrepayResult;
import com.lemnt.third.sdk.wechatpay.WechatPrepayType;
import com.lemnt.third.sdk.wechatpay.WechatRefundCommand;
import com.lemnt.third.sdk.wechatpay.WechatRefundResult;
import com.lemnt.third.sdk.wechatpay.WechatRefundStatus;
import com.lemnt.third.sdk.wechatpay.WechatTransaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 微信渠道：业务参数映射到 SDK，回调与退款结果在这里做业务核验。
 */
class WechatChannelAdapterTest {
    final WechatPayClient client = mock(WechatPayClient.class);
    final WechatChannelAdapter adapter =
        new WechatChannelAdapter() {
            @Override
            WechatPayClient client(PayChannelConfig config) {
                return client;
            }
        };

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
    final PaymentRecord paid =
        new PaymentRecord(
            1L,
            "PAY1",
            "TR1",
            "WECHAT",
            "lemnt-mall",
            "WX-1",
            new BigDecimal("8.00"),
            "SUCCESS",
            "N-1");
    final PaymentRefund refund =
        new PaymentRefund(
            "PR1",
            "RF1",
            "PAY1",
            "TR1",
            "WECHAT",
            new BigDecimal("8.00"),
            "WAITING",
            null);

    @BeforeEach
    void setUp() {
        when(client.appId()).thenReturn("wx-app");
        when(client.mchId()).thenReturn("1900000001");
        when(client.payNotifyUrl()).thenReturn("https://mall.example.com/trade/payment/notify");
    }

    @Test
    void nativePaymentPassesBusinessParamsToSdk() {
        when(client.prepay(any()))
            .thenReturn(
                new WechatPrepayResult(
                    WechatPrepayType.NATIVE, Map.of("codeUrl", "weixin://wxpay/bizpayurl?pr=abc")));

        var prepay = adapter.payment(payment, config, null);

        ArgumentCaptor<WechatPrepayCommand> captor =
            ArgumentCaptor.forClass(WechatPrepayCommand.class);
        verify(client).prepay(captor.capture());
        WechatPrepayCommand command = captor.getValue();
        assertEquals("PAY1", command.outTradeNo());
        assertEquals("TR1", command.orderNo());
        assertEquals(new BigDecimal("8.00"), command.amount());
        assertEquals("https://mall.example.com/trade/payment/notify", command.notifyUrl());
        assertNull(command.openId());
        assertEquals("weixin://wxpay/bizpayurl?pr=abc", prepay.params().get("codeUrl"));
        assertNull(prepay.payUrl());
        assertEquals("WECHAT", prepay.channel());
    }

    @Test
    void jsapiPaymentForwardsPayerOpenId() {
        when(client.prepay(any()))
            .thenReturn(new WechatPrepayResult(WechatPrepayType.JSAPI, Map.of("paySign", "SIGN")));

        var prepay = adapter.payment(payment, config, "OPENID1");

        ArgumentCaptor<WechatPrepayCommand> captor =
            ArgumentCaptor.forClass(WechatPrepayCommand.class);
        verify(client).prepay(captor.capture());
        assertEquals("OPENID1", captor.getValue().openId());
        assertEquals("SIGN", prepay.params().get("paySign"));
    }

    @Test
    void paymentFailsWhenNotifyUrlMissing() {
        when(client.payNotifyUrl()).thenReturn(" ");

        assertThrows(ServiceException.class, () -> adapter.payment(payment, config, null));
        verify(client, never()).prepay(any());
    }

    @Test
    void paymentCallbackMapsSuccessNotificationToYuanAmount() {
        when(client.parseTransactionNotification(any())).thenReturn(transaction());

        var verified = adapter.paymentCallback(config, notification());

        assertEquals("PAY1", verified.paymentNo());
        assertEquals("TR1", verified.orderNo());
        assertEquals("WECHAT", verified.channel());
        assertEquals("WX-1", verified.channelTradeNo());
        assertEquals(new BigDecimal("8.00"), verified.amount());
        assertEquals("WX-1", verified.notifyId());
    }

    @Test
    void paymentCallbackRejectsMismatchedMerchantOrApplication() {
        when(client.parseTransactionNotification(any()))
            .thenReturn(
                new WechatTransaction(
                    "PAY1", "TR1", "WX-1", "wx-app", "1900000002", "SUCCESS", "CNY", 800));
        assertThrows(ServiceException.class, () -> adapter.paymentCallback(config, notification()));

        when(client.parseTransactionNotification(any()))
            .thenReturn(
                new WechatTransaction(
                    "PAY1", "TR1", "WX-1", "wx-other", "1900000001", "SUCCESS", "CNY", 800));
        assertThrows(ServiceException.class, () -> adapter.paymentCallback(config, notification()));
    }

    @Test
    void paymentCallbackRejectsNotificationWithoutSuccessfulTradeState() {
        when(client.parseTransactionNotification(any()))
            .thenReturn(
                new WechatTransaction(
                    "PAY1", "TR1", "WX-1", "wx-app", "1900000001", "NOTPAY", "CNY", 800));

        assertThrows(ServiceException.class, () -> adapter.paymentCallback(config, notification()));
    }

    @Test
    void paymentCallbackRejectsNotificationWithoutOrderNumber() {
        when(client.parseTransactionNotification(any()))
            .thenReturn(
                new WechatTransaction(
                    "PAY1", null, "WX-1", "wx-app", "1900000001", "SUCCESS", "CNY", 800));

        assertThrows(ServiceException.class, () -> adapter.paymentCallback(config, notification()));
    }

    @Test
    void closeFollowsSdkConfirmation() {
        when(client.closeOrder("PAY1")).thenReturn(true);
        assertTrue(adapter.close(payment, config));

        when(client.closeOrder("PAY1")).thenReturn(false);
        assertFalse(adapter.close(payment, config));
    }

    @Test
    void refundSubmitsBusinessParamsAndMapsSuccess() {
        when(client.refund(any()))
            .thenReturn(
                new WechatRefundResult(
                    "PR1", "WR1", "PAY1", new BigDecimal("8.00"), WechatRefundStatus.SUCCESS));

        var result = adapter.refund(refund, paid, config);

        ArgumentCaptor<WechatRefundCommand> captor =
            ArgumentCaptor.forClass(WechatRefundCommand.class);
        verify(client).refund(captor.capture());
        WechatRefundCommand command = captor.getValue();
        assertEquals("PR1", command.outRefundNo());
        assertEquals("PAY1", command.outTradeNo());
        assertEquals(new BigDecimal("8.00"), command.refundAmount());
        assertEquals(new BigDecimal("8.00"), command.totalAmount());
        assertEquals("售后RF1", command.reason());
        assertEquals("SUCCESS", result.status());
        assertEquals(new BigDecimal("8.00"), result.amount());
        assertEquals("WR1", result.channelRefundNo());
        assertEquals("WECHAT", result.channel());
    }

    @Test
    void refundQueryReturnsNullWhenChannelHasNoRefund() {
        when(client.queryRefund("PR1")).thenReturn(null);

        assertNull(adapter.refundQuery(refund, paid, config));
    }

    @Test
    void refundQueryMapsProcessingWithoutChannelRefundNoToWaiting() {
        when(client.queryRefund("PR1"))
            .thenReturn(
                new WechatRefundResult(
                    "PR1", null, "PAY1", new BigDecimal("8.00"), WechatRefundStatus.PROCESSING));

        var result = adapter.refundQuery(refund, paid, config);

        assertEquals("WAITING", result.status());
        assertNull(result.channelRefundNo());
    }

    @Test
    void refundQueryMapsFailedRefundToFailed() {
        when(client.queryRefund("PR1"))
            .thenReturn(
                new WechatRefundResult(
                    "PR1", "WR1", "PAY1", new BigDecimal("8.00"), WechatRefundStatus.FAILED));

        assertEquals("FAILED", adapter.refundQuery(refund, paid, config).status());
    }

    @Test
    void refundChannelAmountMismatchIsRejected() {
        when(client.refund(any()))
            .thenReturn(
                new WechatRefundResult(
                    "PR1", "WR1", "PAY1", new BigDecimal("7.00"), WechatRefundStatus.SUCCESS));

        assertThrows(IllegalStateException.class, () -> adapter.refund(refund, paid, config));
    }

    @Test
    void refundMismatchedPaymentIsRejectedBeforeCallingChannel() {
        PaymentRefund otherChannel =
            new PaymentRefund(
                "PR1",
                "RF1",
                "PAY1",
                "TR1",
                "ALIPAY",
                new BigDecimal("8.00"),
                "WAITING",
                null);

        assertThrows(
            IllegalStateException.class, () -> adapter.refund(otherChannel, paid, config));
        verify(client, never()).refund(any());
    }

    private WechatTransaction transaction() {
        return new WechatTransaction(
            "PAY1", "TR1", "WX-1", "wx-app", "1900000001", "SUCCESS", "CNY", 800);
    }

    private PaymentChannelNotification notification() {
        return new PaymentChannelNotification(
            "{}", "SIGN", "1700000000", "NONCE", "SERIAL", "WECHATPAY2-SHA256-RSA2048");
    }
}
