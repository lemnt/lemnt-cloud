package com.lemnt.trade.infrastructure.payment;

import com.lemnt.trade.domain.payment.model.PaymentRecord;
import com.lemnt.trade.infrastructure.payment.payment.PaymentChannelCloseClient;
import com.lemnt.trade.infrastructure.payment.payment.PaymentChannelCloseGatewayImpl;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentChannelCloseGatewayImplTest {
    final PaymentRecord payment = new PaymentRecord(1L, "P1", "O1", "ALIPAY", null,
            BigDecimal.TEN, "WAITING", null);

    @Test
    void missingAdapterIsFailClosed() {
        assertFalse(new PaymentChannelCloseGatewayImpl(List.of()).closeUnpaid(payment));
    }

    @Test
    void ambiguousAdapterIsFailClosed() {
        var first = mock(PaymentChannelCloseClient.class);
        var second = mock(PaymentChannelCloseClient.class);
        when(first.channel()).thenReturn("ALIPAY");
        when(second.channel()).thenReturn("ALIPAY");
        assertFalse(new PaymentChannelCloseGatewayImpl(List.of(first, second)).closeUnpaid(payment));
        verify(first, never()).closeUnpaid(any());
        verify(second, never()).closeUnpaid(any());
    }
}
