package com.lemnt.pay.infrastructure.paymentrefund;

import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.infrastructure.paymentrefund.channel.SandboxPaymentRefundClient;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 沙箱退款必须与原支付一致，重复调用返回稳定渠道编号。
 */
class SandboxPaymentRefundClientTest {
    final SandboxPaymentRefundClient client = new SandboxPaymentRefundClient();
    final PayChannelConfig config =
        new PayChannelConfig(9L, 1L, "SANDBOX", 0, BigDecimal.ZERO, null, "{}", null);
    final PaymentRecord payment =
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

    @Test
    void refundReturnsStableSuccessForMatchingPayment() {
        var result = client.refund(refund, payment, config);
        assertEquals("SUCCESS", result.status());
        assertEquals("SBR-PR1", result.channelRefundNo());
        assertEquals(new BigDecimal("8.00"), result.amount());
        assertEquals("SBR-PR1", client.refund(refund, payment, config).channelRefundNo());
        assertNull(client.query(refund, payment, config));
    }

    @Test
    void mismatchedOrIncompletePaymentIsRejected() {
        assertThrows(
            IllegalStateException.class,
            () ->
                client.refund(
                    new PaymentRefund(
                        "PR1",
                        "RF1",
                        "PAY1",
                        "TR1",
                        "ALIPAY",
                        new BigDecimal("8.00"),
                        "WAITING",
                        null),
                    payment,
                    config));
        assertThrows(
            IllegalStateException.class,
            () ->
                client.refund(
                    new PaymentRefund(
                        "PR1",
                        "RF1",
                        "PAY1",
                        "TR1",
                        "SANDBOX",
                        new BigDecimal("9.00"),
                        "WAITING",
                        null),
                    payment,
                    config));
        assertThrows(
            IllegalStateException.class,
            () ->
                client.refund(
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
                client.refund(
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
