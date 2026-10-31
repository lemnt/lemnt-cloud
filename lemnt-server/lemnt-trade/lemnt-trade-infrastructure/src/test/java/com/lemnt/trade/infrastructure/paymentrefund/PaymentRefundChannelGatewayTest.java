package com.lemnt.trade.infrastructure.paymentrefund;

import com.lemnt.trade.domain.payment.model.PaymentRecord;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefundResult;
import com.lemnt.trade.infrastructure.paymentrefund.channel.PaymentRefundClient;
import com.lemnt.trade.infrastructure.paymentrefund.gateway.PaymentRefundChannelGatewayImpl;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 渠道缺失和未知结果不能变成成功退款。 */
class PaymentRefundChannelGatewayTest {
    final PaymentRefund refund = new PaymentRefund("PR1", "RF1", "P1", "TR1", "ALIPAY",
            BigDecimal.TEN, "WAITING", null);
    final PaymentRecord payment = new PaymentRecord(1L, "P1", "TR1", "ALIPAY", "TX1",
            BigDecimal.TEN, "SUCCESS", "N1");

    @Test
    void absentOrDuplicateChannelAdaptersCannotPretendRefundSucceeded() {
        assertThrows(IllegalStateException.class,
                () -> new PaymentRefundChannelGatewayImpl(List.of()).refund(refund, payment));
        var client = mock(PaymentRefundClient.class);
        when(client.channel()).thenReturn("ALIPAY");
        assertThrows(IllegalStateException.class,
                () -> new PaymentRefundChannelGatewayImpl(List.of(client, client)).refund(refund, payment));
        verify(client, never()).query(any(), any());
        verify(client, never()).refund(any(), any());
    }

    @Test
    void unknownQueryThrowsAndCannotCauseAnotherSubmission() {
        var client = mock(PaymentRefundClient.class);
        when(client.channel()).thenReturn("ALIPAY");
        when(client.query(refund, payment)).thenThrow(new IllegalStateException("网络超时"));
        assertThrows(IllegalStateException.class,
                () -> new PaymentRefundChannelGatewayImpl(List.of(client)).refund(refund, payment));
        verify(client, never()).refund(any(), any());
    }

    @Test
    void authenticatedChannelResultIsReturnedBeforeAnyResubmission() {
        var client = mock(PaymentRefundClient.class);
        when(client.channel()).thenReturn("ALIPAY");
        var result = new PaymentRefundResult("PR1", "P1", "ALIPAY", BigDecimal.TEN, "SUCCESS", "RTX1");
        when(client.query(refund, payment)).thenReturn(result);
        assertEquals(result, new PaymentRefundChannelGatewayImpl(List.of(client)).refund(refund, payment));
        verify(client, never()).refund(any(), any());
    }
}
