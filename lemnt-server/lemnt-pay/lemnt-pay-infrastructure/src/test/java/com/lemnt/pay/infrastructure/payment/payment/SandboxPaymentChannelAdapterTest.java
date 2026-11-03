package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentRecord;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** 沙箱渠道签名、时间窗、密钥与预支付参数。 */
class SandboxPaymentChannelAdapterTest {
    static final String SECRET = "0123456789abcdef";
    final SandboxPaymentChannelAdapter adapter = new SandboxPaymentChannelAdapter();
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

    private String body() {
        return "{\"paymentNo\":\"PAY1\",\"orderNo\":\"TR1\",\"channelTradeNo\":\"SB-1\","
                + "\"amount\":\"8.00\",\"notifyId\":\"N-1\"}";
    }

    private String signature(String timestamp) {
        return SandboxPaymentChannelAdapter.sign(
                SECRET,
                SandboxPaymentChannelAdapter.canonical(
                        "PAY1", "TR1", new BigDecimal("8.00"), "SB-1", "N-1", timestamp));
    }

    @Test
    void prepayReturnsLocalParamsWithoutSecret() {
        var prepay = adapter.prepay(payment, config);
        assertEquals("PAY1", prepay.paymentNo());
        assertEquals("SANDBOX", prepay.channel());
        assertEquals("lemnt-mall", prepay.params().get("appKey"));
        assertFalse(prepay.params().containsValue(SECRET));
    }

    @Test
    void verifyAcceptsFreshSignedNotification() {
        String time = String.valueOf(System.currentTimeMillis());
        var verified = adapter.verify(config, body(), signature(time), time);
        assertEquals("PAY1", verified.paymentNo());
        assertEquals("TR1", verified.orderNo());
        assertEquals(new BigDecimal("8.00"), verified.amount());
        assertEquals("N-1", verified.notifyId());
    }

    @Test
    void verifyRejectsTamperedAndStaleNotification() {
        String time = String.valueOf(System.currentTimeMillis());
        assertThrows(
                ServiceException.class,
                () ->
                        adapter.verify(
                                config, body().replace("8.00", "9.00"), signature(time), time));
        String stale = String.valueOf(System.currentTimeMillis() - 6 * 60_000L);
        assertThrows(
                ServiceException.class,
                () -> adapter.verify(config, body(), signature(stale), stale));
        assertThrows(
                ServiceException.class,
                () ->
                        adapter.verify(
                                new PayChannelConfig(
                                        1L, 1L, "SANDBOX", 0, BigDecimal.ZERO, null, "{}", null),
                                body(),
                                signature(time),
                                time));
    }

    @Test
    void closeUnpaidOnlyAcceptsSandboxPayment() {
        var close = new SandboxPaymentChannelCloseClient();
        assertTrue(close.closeUnpaid(payment));
        assertFalse(
                close.closeUnpaid(
                        new PaymentRecord(
                                1L,
                                "PAY1",
                                "TR1",
                                "ALIPAY",
                                null,
                                null,
                                BigDecimal.TEN,
                                "WAITING",
                                null)));
    }
}
