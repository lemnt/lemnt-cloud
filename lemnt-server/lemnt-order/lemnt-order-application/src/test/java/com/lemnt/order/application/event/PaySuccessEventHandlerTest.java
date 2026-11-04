package com.lemnt.order.application.event;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.application.event.service.PaySuccessEventHandler;
import com.lemnt.order.application.order.service.OrderLogService;
import com.lemnt.order.application.order.service.OrderPersistenceService;
import com.lemnt.order.application.promotion.service.ActivityOrderLifecycleService;
import com.lemnt.order.domain.event.gateway.MessageInboxGateway;
import com.lemnt.order.domain.event.gateway.OrderEventGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.model.TradeOrder;
import com.lemnt.order.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.order.domain.payment.model.PaymentRecord;
import com.lemnt.pay.client.event.PaySuccessEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 保留原支付通知的金额、幂等和状态约束，拆分后只核验资金事实并更新订单。 */
class PaySuccessEventHandlerTest {
    final MessageInboxGateway inbox = mock(MessageInboxGateway.class);
    final OrderPersistenceService persistence = mock(OrderPersistenceService.class);
    final TradeOrderGateway orders = mock(TradeOrderGateway.class);
    final TradePaymentGateway payments = mock(TradePaymentGateway.class);
    final OrderEventGateway events = mock(OrderEventGateway.class);
    final ActivityOrderLifecycleService activities = mock(ActivityOrderLifecycleService.class);
    final PaySuccessEventHandler handler =
            new PaySuccessEventHandler(
                    inbox,
                    persistence,
                    orders,
                    payments,
                    mock(OrderLogService.class),
                    activities,
                    events);
    final CommerceEventEnvelope notice =
            new CommerceEventEnvelope(
                    "E1",
                    CommerceEventTopics.PAY_SUCCESS,
                    1,
                    "PAY1",
                    "TR1",
                    Instant.now(),
                    null,
                    JsonUtils.toJsonStr(
                            new PaySuccessEvent(
                                    "PAY1", "TR1", 2L, "ALIPAY", BigDecimal.TEN, "CHANNEL1")));

    @BeforeEach
    void setup() {
        when(inbox.begin(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);
        state("PENDING_PAYMENT");
        payment("SUCCESS", "CHANNEL1", BigDecimal.TEN);
    }

    void state(String state) {
        when(persistence.findByOrderNoForUpdate("TR1"))
                .thenReturn(new TradeOrder(1L, "TR1", 2L, BigDecimal.TEN, state, List.of(), null));
    }

    void payment(String state, String tradeNo, BigDecimal amount) {
        when(payments.findByOrderNo("TR1"))
                .thenReturn(
                        List.of(
                                new PaymentRecord(
                                        1L, "PAY1", "TR1", "ALIPAY", null, tradeNo, amount, state,
                                        "N")));
    }

    @Test
    void sameSuccessfulChannelTransactionIsIdempotentWithNewNotificationId() {
        when(inbox.begin(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);
        state("PAID");
        handler.execute(notice);
        verifyNoInteractions(events);
        verify(orders, never()).transitionStatus(any(), any(), any());
    }

    @Test
    void mismatchedAmountOrChannelTransactionIsRejected() {
        payment("SUCCESS", "CHANNEL1", BigDecimal.ONE);
        assertThrows(IllegalStateException.class, () -> handler.execute(notice));
        payment("SUCCESS", "OTHER", BigDecimal.TEN);
        assertThrows(IllegalStateException.class, () -> handler.execute(notice));
        verifyNoInteractions(events);
    }

    @Test
    void successWritesOrderAndBusinessEventInOrder() {
        when(orders.transitionStatus("TR1", "PENDING_PAYMENT", "PAID")).thenReturn(true);
        handler.execute(notice);
        var sequence = inOrder(persistence, payments, orders, activities, events);
        sequence.verify(persistence).findByOrderNoForUpdate("TR1");
        sequence.verify(payments).findByOrderNo("TR1");
        sequence.verify(orders).transitionStatus("TR1", "PENDING_PAYMENT", "PAID");
        sequence.verify(activities).paid("TR1");
        sequence.verify(events).paid(any());
    }

    @Test
    void failedStatusTransitionCannotAppendConfirmationEvent() {
        assertThrows(IllegalStateException.class, () -> handler.execute(notice));
        verifyNoInteractions(events);
    }

    @Test
    void successfulReplayRejectsInconsistentOrderStatesWithoutSideEffects() {
        for (String s : List.of("RESERVING", "CLOSED", "PAID")) {
            state(s);
            assertThrows(IllegalStateException.class, () -> handler.execute(notice));
        }
        verify(orders, never()).transitionStatus(any(), any(), any());
        verifyNoInteractions(events);
    }

    @Test
    void successfulReplayRemainsIdempotentAfterShippingAndReceipt() {
        when(inbox.begin(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);
        for (String s : List.of("SHIPPED", "COMPLETED")) {
            state(s);
            handler.execute(notice);
        }
        verify(orders, never()).transitionStatus(any(), any(), any());
        verifyNoInteractions(events);
    }
}
