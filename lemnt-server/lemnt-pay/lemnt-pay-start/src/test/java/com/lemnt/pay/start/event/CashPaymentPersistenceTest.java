package com.lemnt.pay.start.event;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.event.OrderClosedEvent;
import com.lemnt.pay.application.event.service.OrderClosedEventHandler;
import com.lemnt.pay.application.payment.service.PaymentNotificationService;
import com.lemnt.pay.application.paymentrefund.service.PaymentRefundService;
import com.lemnt.pay.domain.event.gateway.MessageInboxGateway;
import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.order.model.FrozenRefundPlan;
import com.lemnt.pay.domain.order.model.OrderPaymentSnapshot;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PaymentOperator;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;
import com.lemnt.pay.infrastructure.event.gateway.MessageInboxGatewayImpl;
import com.lemnt.pay.infrastructure.event.mapper.MessageInboxMapper;
import com.lemnt.pay.infrastructure.payment.gateway.TradePaymentGatewayImpl;
import com.lemnt.pay.infrastructure.payment.mapper.TradePaymentMapper;
import com.lemnt.pay.infrastructure.paymentrefund.gateway.PaymentRefundGatewayImpl;
import com.lemnt.pay.infrastructure.paymentrefund.mapper.PaymentRefundMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 支付库没有任何订单/售后表，验证资金和事件原子性及退款额度。 */
class CashPaymentPersistenceTest extends OutboxPersistenceTest {
    OrderPaymentGateway orders;
    TradePaymentGateway payments;
    PaymentRefundGateway refunds;
    PaymentNotificationService notifications;
    PaymentRefundService refundService;

    @BeforeEach
    void cashSetup() {
        orders = mock(OrderPaymentGateway.class);
        when(orders.findByOrderNo("O1"))
                .thenReturn(
                        new OrderPaymentSnapshot(
                                "O1", 7L, new BigDecimal("10.00"), "PENDING_PAYMENT"));
        payments =
                proxy(
                        new TradePaymentGatewayImpl(session.getMapper(TradePaymentMapper.class)),
                        TradePaymentGateway.class);
        refunds =
                proxy(
                        new PaymentRefundGatewayImpl(session.getMapper(PaymentRefundMapper.class)),
                        PaymentRefundGateway.class);
        notifications =
                proxy(
                        new PaymentNotificationService(orders, payments, outbox),
                        PaymentNotificationService.class);
        refundService =
                proxy(
                        new PaymentRefundService(orders, payments, refunds, outbox),
                        PaymentRefundService.class);
        payments.createWaiting("P1", "O1", "SANDBOX", "app", new BigDecimal("10.00"));
    }

    VerifiedPaymentNotification notice(String channelNo) {
        return new VerifiedPaymentNotification(
                "P1", "O1", "SANDBOX", channelNo, new BigDecimal("10.00"), "NOTICE1");
    }

    FrozenRefundPlan plan(String no, String amount, String state) {
        var result = new FrozenRefundPlan(no, "O1", 7L, "P1", new BigDecimal(amount), 1, state);
        when(orders.prepareRefund(eq(no), any(PaymentOperator.class))).thenReturn(result);
        when(orders.refundPlan(no)).thenReturn(result);
        return result;
    }

    @Test
    void callbackWritesOnlyPaymentAndOneFrozenEventAndRejectsDifferentChannelReplay() {
        notifications.execute(notice("CH1"));
        notifications.execute(notice("CH1"));
        assertEquals("SUCCESS", payments.findByPaymentNo("P1").status());
        assertEquals(
                1, jdbc.queryForObject("SELECT COUNT(*) FROM pay_message_outbox", Integer.class));
        assertThrows(IllegalStateException.class, () -> notifications.execute(notice("CH2")));
        assertEquals("CH1", payments.findByPaymentNo("P1").channelTradeNo());
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('ORDER_INFO','ORDER_AFTER_SALE','TRADE_ORDER')",
                        Integer.class));
    }

    @Test
    void failedEventRegistrationRollsBackCashWriteInsteadOfLosingNotification() {
        transaction.executeWithoutResult(
                status -> outbox.enqueue("PAY_SUCCESS", "P1", "O1", "{\"tampered\":true}"));
        assertThrows(IllegalStateException.class, () -> notifications.execute(notice("CH1")));
        assertEquals("WAITING", payments.findByPaymentNo("P1").status());
    }

    @Test
    void genuineLatePaymentAfterOrderCloseIsRetainedForReconciliation() {
        payments.markClosed("P1");
        when(orders.findByOrderNo("O1"))
                .thenReturn(new OrderPaymentSnapshot("O1", 7L, new BigDecimal("10.00"), "CLOSED"));
        notifications.execute(notice("CH1"));
        assertEquals("SUCCESS", payments.findByPaymentNo("P1").status());
        assertEquals(
                1, jdbc.queryForObject("SELECT COUNT(*) FROM pay_message_outbox", Integer.class));
    }

    @Test
    void frozenRefundIsIdempotentAndCashCompletionPublishesOnceWithoutOrderTables() {
        notifications.execute(notice("CH1"));
        plan("AF1", "3.32", "REFUNDING");
        var first = refundService.prepare("AF1");
        assertEquals(first.refundNo(), refundService.prepare("AF1").refundNo());
        var result =
                new PaymentRefundResult(
                        first.refundNo(),
                        "P1",
                        "SANDBOX",
                        new BigDecimal("3.32"),
                        "SUCCESS",
                        "RCH1");
        assertEquals("SUCCESS", refundService.complete(first, result).status());
        assertEquals("SUCCESS", refundService.complete(first, result).status());
        assertEquals(
                1, jdbc.queryForObject("SELECT COUNT(*) FROM pay_payment_refund", Integer.class));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM pay_message_outbox WHERE event_type='PAY_REFUND_SUCCESS'",
                        Integer.class));
        plan("AF2", "6.69", "REFUNDING");
        assertThrows(IllegalStateException.class, () -> refundService.prepare("AF2"));
        plan("AF1", "3.33", "REFUNDING");
        assertThrows(IllegalStateException.class, () -> refundService.prepare("AF1"));
    }

    @Test
    void zeroCashUsesOrderResultWithoutCreatingPaymentRefundOrFundingEvent() {
        plan("AF0", "0.00", "REFUNDED");
        var zero = refundService.prepare("AF0");
        assertNull(zero.refundNo());
        assertEquals("NONE", zero.channel());
        assertEquals("SUCCESS", zero.status());
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM pay_payment_refund", Integer.class));
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM pay_message_outbox", Integer.class));
    }

    @Test
    void closeEventReceiptRollsBackOnWrongOwnerAndRetryClosesOnlyWaitingPayment() {
        var inbox =
                proxy(
                        new MessageInboxGatewayImpl(
                                session.getMapper(
                                        MessageInboxMapper
                                                .class)),
                        MessageInboxGateway.class);
        var handler =
                proxy(
                        new OrderClosedEventHandler(
                                inbox, orders, payments),
                        OrderClosedEventHandler.class);
        var payload =
                JsonUtils.toJsonStr(
                        new OrderClosedEvent(
                                "O1", 7L, List.of()));
        var event =
                new CommerceEventEnvelope(
                        "EC1",
                        "ORDER_CLOSED",
                        1,
                        "O1",
                        "O1",
                        Instant.now(),
                        null,
                        payload);
        when(orders.findByOrderNo("O1"))
                .thenReturn(new OrderPaymentSnapshot("O1", 8L, new BigDecimal("10.00"), "CLOSED"));
        assertThrows(IllegalStateException.class, () -> handler.execute(event));
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM pay_message_inbox", Integer.class));
        assertEquals("WAITING", payments.findByPaymentNo("P1").status());
        when(orders.findByOrderNo("O1"))
                .thenReturn(new OrderPaymentSnapshot("O1", 7L, new BigDecimal("10.00"), "CLOSED"));
        handler.execute(event);
        handler.execute(event);
        assertEquals("CLOSED", payments.findByPaymentNo("P1").status());
        assertEquals(
                1, jdbc.queryForObject("SELECT COUNT(*) FROM pay_message_inbox", Integer.class));
    }
}
