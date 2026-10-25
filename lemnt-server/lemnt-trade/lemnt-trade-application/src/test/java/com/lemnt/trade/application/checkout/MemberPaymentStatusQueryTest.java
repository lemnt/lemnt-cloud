package com.lemnt.trade.application.checkout;

import com.lemnt.trade.application.checkout.executor.MemberPaymentStatusQueryExe;
import com.lemnt.trade.application.checkout.qry.MemberPaymentStatusQry;
import com.lemnt.trade.domain.gateway.*;
import com.lemnt.trade.domain.model.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberPaymentStatusQueryTest {
    final TradeOrderGateway orders = mock(TradeOrderGateway.class);
    final TradePaymentGateway payments = mock(TradePaymentGateway.class);
    final MemberPaymentStatusQueryExe query = new MemberPaymentStatusQueryExe(orders, payments);

    void order(String state, Long member) {
        when(orders.findByOrderNo("TR1"))
                .thenReturn(
                        new TradeOrder(1L, "TR1", member, BigDecimal.TEN, state, List.of(), null));
    }

    PaymentRecord payment(String state, BigDecimal amount) {
        return new PaymentRecord(1L, "P1", "TR1", "WECHAT", "TX", amount, state, "N");
    }

    String status() {
        return query.execute(new MemberPaymentStatusQry(7L, "TR1")).localPaymentStatus();
    }

    @Test
    void foreignAndMissingOrdersDoNotReadPaymentRecords() {
        assertNull(query.execute(new MemberPaymentStatusQry(7L, "TR1")));
        order("PAID", 8L);
        assertNull(query.execute(new MemberPaymentStatusQry(7L, "TR1")));
        verifyNoInteractions(payments);
    }

    @Test
    void localWaitingDoesNotBecomeVerifiedChannelUnpaid() {
        order("PENDING_PAYMENT", 7L);
        when(payments.findByOrderNo("TR1")).thenReturn(List.of(payment("WAITING", BigDecimal.TEN)));
        var value = query.execute(new MemberPaymentStatusQry(7L, "TR1"));
        assertEquals("WAITING", value.localPaymentStatus());
        assertFalse(value.channelVerified());
        order("CLOSED", 7L);
        assertEquals("RECONCILE_REQUIRED", status());
        when(payments.findByOrderNo("TR1")).thenReturn(List.of(payment("CLOSED", BigDecimal.TEN)));
        assertEquals("CLOSED", status());
    }

    @Test
    void paidShippedAndCompletedRequireOneMatchingSuccessfulPayment() {
        when(payments.findByOrderNo("TR1")).thenReturn(List.of(payment("SUCCESS", BigDecimal.TEN)));
        for (String state : List.of("PAID", "SHIPPED", "COMPLETED")) {
            order(state, 7L);
            assertEquals("SUCCESS", status());
        }
        order("PENDING_PAYMENT", 7L);
        assertEquals("RECONCILE_REQUIRED", status());
    }

    @Test
    void inconsistentAmountsStatesAndMultipleSuccessesRequireReconciliation() {
        order("PAID", 7L);
        for (var records :
                List.of(
                        List.<PaymentRecord>of(),
                        List.of(payment("SUCCESS", BigDecimal.ONE)),
                        List.of(payment("UNKNOWN", BigDecimal.TEN)),
                        List.of(
                                payment("SUCCESS", BigDecimal.TEN),
                                payment("SUCCESS", BigDecimal.TEN)))) {
            when(payments.findByOrderNo("TR1")).thenReturn(records);
            assertEquals("RECONCILE_REQUIRED", status());
        }
        order("RESERVING", 7L);
        when(payments.findByOrderNo("TR1")).thenReturn(List.of());
        assertEquals("NOT_CREATED", status());
        order("UNKNOWN", 7L);
        assertEquals("RECONCILE_REQUIRED", status());
        verify(payments, never()).markSuccess(any());
        verify(payments, never()).markClosed(anyString());
    }
}
