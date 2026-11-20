package com.lemnt.pay.infrastructure.channel.gateway;

import com.lemnt.pay.domain.channel.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.infrastructure.channel.adapter.AbstractChannelAdapter;
import com.lemnt.pay.infrastructure.channel.factory.PaymentChannelFactory;
import com.lemnt.pay.infrastructure.channel.support.PayChannelConfigResolver;

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
    void missingAdapterIsRejected() {
        assertThrows(
            IllegalArgumentException.class, () -> gateway(List.of()).closeUnpaid(payment));
    }

    @Test
    void duplicateAdapterFailsAtStartup() {
        var first = mock(AbstractChannelAdapter.class);
        var second = mock(AbstractChannelAdapter.class);
        when(first.channel()).thenReturn("ALIPAY");
        when(second.channel()).thenReturn("ALIPAY");

        assertThrows(
            IllegalStateException.class, () -> new PaymentChannelFactory(List.of(first, second)));
    }

    @Test
    void missingChannelConfigIsFailClosed() {
        var adapter = mock(AbstractChannelAdapter.class);
        when(adapter.channel()).thenReturn("ALIPAY");

        assertFalse(gateway(List.of(adapter)).closeUnpaid(payment));
        verify(adapter, never()).close(any(), any());
    }

    @Test
    void configuredChannelDelegatesWithResolvedConfig() {
        var adapter = mock(AbstractChannelAdapter.class);
        var config =
            new PayChannelConfig(9L, 1L, "ALIPAY", 0, BigDecimal.ZERO, null, "{}", null);
        when(adapter.channel()).thenReturn("ALIPAY");
        when(adapter.close(any(), any())).thenReturn(true);
        when(configs.resolve("APP1", "ALIPAY")).thenReturn(config);
        PaymentRecord bound =
            new PaymentRecord(
                1L, "P1", "O1", "ALIPAY", "APP1", null, BigDecimal.TEN, "WAITING", null);

        assertTrue(gateway(List.of(adapter)).closeUnpaid(bound));
        verify(adapter).close(bound, config);
    }

    private PaymentChannelCloseGatewayImpl gateway(List<AbstractChannelAdapter> adapters) {
        return new PaymentChannelCloseGatewayImpl(
            new PaymentChannelFactory(adapters), configs);
    }
}
