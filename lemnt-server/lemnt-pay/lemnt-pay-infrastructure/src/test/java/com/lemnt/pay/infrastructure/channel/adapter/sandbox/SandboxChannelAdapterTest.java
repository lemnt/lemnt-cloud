package com.lemnt.pay.infrastructure.channel.adapter.sandbox;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.domain.channel.model.PayChannelConfig;
import com.lemnt.pay.domain.channel.model.PaymentChannelNotification;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 沙箱渠道：本地预支付、HMAC 通知验签、关单与退款幂等。
 */
class SandboxChannelAdapterTest {
    static final String SECRET = "0123456789abcdef";
    final SandboxChannelAdapter adapter = new SandboxChannelAdapter();
    final PayChannelConfig config =
        new PayChannelConfig(
            1L,
            1L,
            "SANDBOX",
            0,
            BigDecimal.ZERO,
            null,
            "{\"hmacSecret\":\"" + SECRET + "\"}",
            null);
    final PaymentRecord payment =
        new PaymentRecord(
            1L,
            "PAY1",
            "TR1",
            "SANDBOX",
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
            "SANDBOX",
            "lemnt-mall",
            "SB-1",
            new BigDecimal("8.00"),
            "SUCCESS",
            "N-1");
    final PaymentRefund refund =
        new PaymentRefund(
            "PR1",
            "RF1",
            "PAY1",
            "TR1",
            "SANDBOX",
            new BigDecimal("8.00"),
            "WAITING",
            null);

    private String body() {
        return "{\"paymentNo\":\"PAY1\",\"orderNo\":\"TR1\",\"channelTradeNo\":\"SB-1\","
            + "\"amount\":\"8.00\",\"notifyId\":\"N-1\"}";
    }

    private String signature(String timestamp) {
        return SandboxChannelAdapter.sign(
            SECRET,
            SandboxChannelAdapter.canonical(
                "PAY1", "TR1", new BigDecimal("8.00"), "SB-1", "N-1", timestamp));
    }

    private PaymentChannelNotification notify(
        String body, String signature, String timestamp) {
        return new PaymentChannelNotification(body, signature, timestamp, null, null, null);
    }

    @Test
    void paymentReturnsLocalParamsWithoutSecret() {
        var prepay = adapter.payment(payment, config, null);
        assertEquals("PAY1", prepay.paymentNo());
        assertEquals("SANDBOX", prepay.channel());
        assertEquals("lemnt-mall", prepay.params().get("appKey"));
        assertFalse(prepay.params().containsValue(SECRET));
    }

    @Test
    void paymentCallbackAcceptsFreshSignedNotification() {
        String time = String.valueOf(System.currentTimeMillis());
        var verified = adapter.paymentCallback(config, notify(body(), signature(time), time));
        assertEquals("PAY1", verified.paymentNo());
        assertEquals("TR1", verified.orderNo());
        assertEquals(new BigDecimal("8.00"), verified.amount());
        assertEquals("N-1", verified.notifyId());
    }

    @Test
    void paymentCallbackRejectsTamperedAndStaleNotification() {
        String time = String.valueOf(System.currentTimeMillis());
        assertThrows(
            ServiceException.class,
            () ->
                adapter.paymentCallback(
                    config, notify(body().replace("8.00", "9.00"), signature(time), time)));
        String stale = String.valueOf(System.currentTimeMillis() - 6 * 60_000L);
        assertThrows(
            ServiceException.class,
            () -> adapter.paymentCallback(config, notify(body(), signature(stale), stale)));
        assertThrows(
            ServiceException.class,
            () ->
                adapter.paymentCallback(
                    new PayChannelConfig(
                        1L, 1L, "SANDBOX", 0, BigDecimal.ZERO, null, "{}", null),
                    notify(body(), signature(time), time)));
    }

    @Test
    void closeOnlyAcceptsSandboxPayment() {
        assertTrue(adapter.close(payment, config));
        assertFalse(
            adapter.close(
                new PaymentRecord(
                    1L,
                    "PAY1",
                    "TR1",
                    "ALIPAY",
                    null,
                    null,
                    BigDecimal.TEN,
                    "WAITING",
                    null),
                config));
    }

    @Test
    void refundReturnsStableSuccessForMatchingPayment() {
        var result = adapter.refund(refund, paid, config);
        assertEquals("SUCCESS", result.status());
        assertEquals("SBR-PR1", result.channelRefundNo());
        assertEquals(new BigDecimal("8.00"), result.amount());
        assertEquals("SBR-PR1", adapter.refund(refund, paid, config).channelRefundNo());
        assertNull(adapter.refundQuery(refund, paid, config));
    }

    @Test
    void refundRejectsMismatchedOrIncompletePayment() {
        assertThrows(
            IllegalStateException.class,
            () ->
                adapter.refund(
                    new PaymentRefund(
                        "PR1",
                        "RF1",
                        "PAY1",
                        "TR1",
                        "ALIPAY",
                        new BigDecimal("8.00"),
                        "WAITING",
                        null),
                    paid,
                    config));
        assertThrows(
            IllegalStateException.class,
            () ->
                adapter.refund(
                    new PaymentRefund(
                        "PR1",
                        "RF1",
                        "PAY1",
                        "TR1",
                        "SANDBOX",
                        new BigDecimal("9.00"),
                        "WAITING",
                        null),
                    paid,
                    config));
        assertThrows(
            IllegalStateException.class,
            () ->
                adapter.refund(
                    refund,
                    new PaymentRecord(
                        1L,
                        "PAY1",
                        "TR1",
                        "SANDBOX",
                        "lemnt-mall",
                        null,
                        new BigDecimal("8.00"),
                        "SUCCESS",
                        "N-1"),
                    config));
        assertThrows(
            IllegalStateException.class,
            () ->
                adapter.refund(
                    refund,
                    new PaymentRecord(
                        1L,
                        "PAY1",
                        "TR1",
                        "SANDBOX",
                        "lemnt-mall",
                        "SB-1",
                        new BigDecimal("8.00"),
                        "WAITING",
                        "N-1"),
                    config));
    }
}
