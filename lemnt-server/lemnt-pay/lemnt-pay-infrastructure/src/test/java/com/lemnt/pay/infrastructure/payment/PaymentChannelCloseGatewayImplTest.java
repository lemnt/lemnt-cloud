package com.lemnt.pay.infrastructure.payment;

import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.infrastructure.payment.payment.PayChannelConfigResolver;
import com.lemnt.pay.infrastructure.payment.payment.PaymentChannelCloseClient;
import com.lemnt.pay.infrastructure.payment.payment.PaymentChannelCloseGatewayImpl;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentChannelCloseGatewayImplTest {
    final PayChannelConfigResolver configs = mock(PayChannelConfigResolver.class);
    final PaymentRecord payment =
        new PaymentRecord(
            1L, "P1", "O1", "ALIPAY", null, null, BigDecimal.TEN, "WAITING", null);

    @Test
    void missingAdapterIsFailClosed() {
        assertFalse(gateway(List.of()).closeUnpaid(payment));
    }

    @Test
    void ambiguousAdapterIsFailClosed() {
        var first = mock(PaymentChannelCloseClient.class);
        var second = mock(PaymentChannelCloseClient.class);
        when(first.channel()).thenReturn("ALIPAY");
        when(second.channel()).thenReturn("ALIPAY");
        assertFalse(gateway(List.of(first, second)).closeUnpaid(payment));
        verify(first, never()).closeUnpaid(any(), any());
        verify(second, never()).closeUnpaid(any(), any());
    }

    @Test
    void missingChannelConfigIsFailClosed() {
        var client = mock(PaymentChannelCloseClient.class);
        when(client.channel()).thenReturn("ALIPAY");
        assertFalse(gateway(List.of(client)).closeUnpaid(payment));
        verify(client, never()).closeUnpaid(any(), any());
    }

    @Test
    void configuredChannelDelegatesWithResolvedConfig() {
        var client = mock(PaymentChannelCloseClient.class);
        var config =
            new PayChannelConfig(9L, 1L, "ALIPAY", 0, BigDecimal.ZERO, null, "{}", null);
        when(client.channel()).thenReturn("ALIPAY");
        when(client.closeUnpaid(any(), any())).thenReturn(true);
        when(configs.resolve("APP1", "ALIPAY")).thenReturn(config);
        PaymentRecord bound =
            new PaymentRecord(
                1L, "P1", "O1", "ALIPAY", "APP1", null, BigDecimal.TEN, "WAITING", null);

        assertTrue(gateway(List.of(client)).closeUnpaid(bound));
        verify(client).closeUnpaid(bound, config);
    }

    private PaymentChannelCloseGatewayImpl gateway(List<PaymentChannelCloseClient> clients) {
        return new PaymentChannelCloseGatewayImpl(clients, configs);
    }
}
