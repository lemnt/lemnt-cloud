package com.lemnt.pay.application.checkout;

import com.lemnt.pay.application.payment.executor.MemberPaymentStatusQueryExe;
import com.lemnt.pay.application.payment.qry.MemberPaymentStatusQry;
import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.order.model.OrderPaymentSnapshot;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PaymentRecord;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberPaymentStatusQueryTest {
    final OrderPaymentGateway orderPersistenceService = mock(OrderPaymentGateway.class);
    final TradePaymentGateway payments = mock(TradePaymentGateway.class);
    final MemberPaymentStatusQueryExe query =
        new MemberPaymentStatusQueryExe(orderPersistenceService, payments);

    void order(String state, Long member) {
        when(orderPersistenceService.findByOrderNo("TR1"))
            .thenReturn(new OrderPaymentSnapshot("TR1", member, BigDecimal.TEN, state));
    }

    PaymentRecord payment(String state, BigDecimal amount) {
        return new PaymentRecord(1L, "P1", "TR1", "WECHAT", null, "TX", amount, state, "N");
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
