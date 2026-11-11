package com.lemnt.pay.infrastructure.paymentrefund;

import com.lemnt.pay.domain.channel.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;
import com.lemnt.pay.infrastructure.channel.adapter.AbstractChannelAdapter;
import com.lemnt.pay.infrastructure.channel.factory.PaymentChannelFactory;
import com.lemnt.pay.infrastructure.channel.support.PayChannelConfigResolver;
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
    void absentChannelCannotPretendRefundSucceeded() {
        assertThrows(
            IllegalArgumentException.class, () -> gateway(List.of()).refund(refund, payment));
    }

    @Test
    void duplicateChannelAdaptersFailAtStartup() {
        var adapter = mock(AbstractChannelAdapter.class);
        when(adapter.channel()).thenReturn("ALIPAY");

        assertThrows(
            IllegalStateException.class,
            () -> new PaymentChannelFactory(List.of(adapter, adapter)));
    }

    @Test
    void missingChannelConfigCannotPretendRefundSucceeded() {
        var adapter = mock(AbstractChannelAdapter.class);
        when(adapter.channel()).thenReturn("ALIPAY");

        assertThrows(
            IllegalStateException.class,
            () ->
                new PaymentRefundChannelGatewayImpl(
                        new PaymentChannelFactory(List.of(adapter)), configs)
                    .refund(refund, payment));
        verify(adapter, never()).refundQuery(any(), any(), any());
        verify(adapter, never()).refund(any(), any(), any());
    }

    @Test
    void unknownQueryThrowsAndCannotCauseAnotherSubmission() {
        var adapter = mock(AbstractChannelAdapter.class);
        when(adapter.channel()).thenReturn("ALIPAY");
        when(adapter.refundQuery(refund, payment, config))
            .thenThrow(new IllegalStateException("网络超时"));

        assertThrows(
            IllegalStateException.class, () -> gateway(List.of(adapter)).refund(refund, payment));
        verify(adapter, never()).refund(any(), any(), any());
    }

    @Test
    void authenticatedChannelResultIsReturnedBeforeAnyResubmission() {
        var adapter = mock(AbstractChannelAdapter.class);
        when(adapter.channel()).thenReturn("ALIPAY");
        var result =
            new PaymentRefundResult("PR1", "P1", "ALIPAY", BigDecimal.TEN, "SUCCESS", "RTX1");
        when(adapter.refundQuery(refund, payment, config)).thenReturn(result);

        assertEquals(result, gateway(List.of(adapter)).refund(refund, payment));
        verify(adapter, never()).refund(any(), any(), any());
    }

    @Test
    void acceptedButInvisibleRefundStaysWaitingWithoutResubmission() {
        var adapter = mock(AbstractChannelAdapter.class);
        when(adapter.channel()).thenReturn("ALIPAY");
        when(adapter.refundQuery(any(), any(), any())).thenReturn(null);
        PaymentRefund accepted =
            new PaymentRefund(
                "PR1", "RF1", "P1", "TR1", "ALIPAY", BigDecimal.TEN, "WAITING", "ACCEPTED");

        var result = gateway(List.of(adapter)).refund(accepted, payment);

        assertEquals("WAITING", result.status());
        assertEquals("ACCEPTED", result.channelRefundNo());
        verify(adapter, never()).refund(any(), any(), any());
    }

    private PaymentRefundChannelGatewayImpl gateway(List<AbstractChannelAdapter> adapters) {
        when(configs.resolve(payment.appKey(), payment.channel())).thenReturn(config);
        return new PaymentRefundChannelGatewayImpl(
            new PaymentChannelFactory(adapters), configs);
    }
}
