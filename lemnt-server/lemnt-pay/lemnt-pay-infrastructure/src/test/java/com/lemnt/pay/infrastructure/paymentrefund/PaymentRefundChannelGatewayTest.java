package com.lemnt.pay.infrastructure.paymentrefund;

import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;
import com.lemnt.pay.infrastructure.payment.payment.PayChannelConfigResolver;
import com.lemnt.pay.infrastructure.paymentrefund.channel.PaymentRefundClient;
import com.lemnt.pay.infrastructure.paymentrefund.gateway.PaymentRefundChannelGatewayImpl;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 渠道缺失、配置缺失和未知结果不能变成成功退款。
 */
class PaymentRefundChannelGatewayTest {
    final PayChannelConfigResolver configs = mock(PayChannelConfigResolver.class);
    final PayChannelConfig config =
        new PayChannelConfig(9L, 1L, "ALIPAY", 0, BigDecimal.ZERO, null, "{}", null);
    final PaymentRefund refund =
        new PaymentRefund("PR1", "RF1", "P1", "TR1", "ALIPAY", BigDecimal.TEN, "WAITING", null);
    final PaymentRecord payment =
        new PaymentRecord(
            1L, "P1", "TR1", "ALIPAY", null, "TX1", BigDecimal.TEN, "SUCCESS", "N1");

    @Test
    void absentOrDuplicateChannelAdaptersCannotPretendRefundSucceeded() {
        assertThrows(
            IllegalStateException.class,
            () -> gateway(List.of()).refund(refund, payment));
        var client = mock(PaymentRefundClient.class);
        when(client.channel()).thenReturn("ALIPAY");
        assertThrows(
            IllegalStateException.class,
            () -> gateway(List.of(client, client)).refund(refund, payment));
        verify(client, never()).query(any(), any(), any());
        verify(client, never()).refund(any(), any(), any());
    }

    @Test
    void missingChannelConfigCannotPretendRefundSucceeded() {
        var client = mock(PaymentRefundClient.class);
        when(client.channel()).thenReturn("ALIPAY");

        assertThrows(
            IllegalStateException.class,
            () ->
                new PaymentRefundChannelGatewayImpl(List.of(client), configs)
                    .refund(refund, payment));
        verify(client, never()).query(any(), any(), any());
        verify(client, never()).refund(any(), any(), any());
    }

    @Test
    void unknownQueryThrowsAndCannotCauseAnotherSubmission() {
        var client = mock(PaymentRefundClient.class);
        when(client.channel()).thenReturn("ALIPAY");
        when(client.query(refund, payment, config)).thenThrow(new IllegalStateException("网络超时"));
        assertThrows(
            IllegalStateException.class,
            () -> gateway(List.of(client)).refund(refund, payment));
        verify(client, never()).refund(any(), any(), any());
    }

    @Test
    void authenticatedChannelResultIsReturnedBeforeAnyResubmission() {
        var client = mock(PaymentRefundClient.class);
        when(client.channel()).thenReturn("ALIPAY");
        var result =
            new PaymentRefundResult("PR1", "P1", "ALIPAY", BigDecimal.TEN, "SUCCESS", "RTX1");
        when(client.query(refund, payment, config)).thenReturn(result);
        assertEquals(result, gateway(List.of(client)).refund(refund, payment));
        verify(client, never()).refund(any(), any(), any());
    }

    private PaymentRefundChannelGatewayImpl gateway(List<PaymentRefundClient> clients) {
        when(configs.resolve(payment.appKey(), payment.channel())).thenReturn(config);
        return new PaymentRefundChannelGatewayImpl(clients, configs);
    }
}
