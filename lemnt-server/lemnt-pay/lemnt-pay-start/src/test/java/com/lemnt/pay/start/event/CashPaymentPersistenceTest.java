package com.lemnt.pay.start.event;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.event.OrderClosedEvent;
import com.lemnt.pay.application.event.service.OrderClosedEventHandler;
import com.lemnt.pay.application.payment.executor.TradePaymentCreateCmdExe;
import com.lemnt.pay.application.payment.service.PaymentNotificationService;
import com.lemnt.pay.application.paymentrefund.service.PaymentRefundService;
import com.lemnt.pay.domain.channel.gateway.PaymentChannelCloseGateway;
import com.lemnt.pay.domain.event.gateway.MessageInboxGateway;
import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.order.model.FrozenRefundPlan;
import com.lemnt.pay.domain.order.model.OrderPaymentSnapshot;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PaymentOperator;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;
import com.lemnt.pay.infrastructure.event.gateway.InboundEventGatewayImpl;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 支付库没有任何订单/售后表，验证资金和事件原子性及退款额度。
 */
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
        payments.createWaiting("P1", "O1", 9L, "SANDBOX", "app", new BigDecimal("10.00"));
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
    void wrongOwnerRollsBackFenceAndReplayClosesOnlyWaitingPayment() {
        var inbox =
                proxy(
                        new MessageInboxGatewayImpl(session.getMapper(MessageInboxMapper.class)),
                        MessageInboxGateway.class);
        var channels = mock(PaymentChannelCloseGateway.class);
        when(channels.closeUnpaid(any())).thenReturn(true);
        var handler =
                proxy(
                        new OrderClosedEventHandler(
                                inbox, orders, payments, channels, new InboundEventGatewayImpl()),
                        OrderClosedEventHandler.class);
        var payload = JsonUtils.toJsonStr(new OrderClosedEvent("O1", 7L, List.of()));
        var event =
                new CommerceEventEnvelope(
                        "EC1", "ORDER_CLOSED", 1, "O1", "O1", Instant.now(), null, payload);
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
        verify(channels).closeUnpaid(any());
        assertEquals("CLOSED", payments.findByPaymentNo("P1").status());
        assertEquals(
                1, jdbc.queryForObject("SELECT COUNT(*) FROM pay_message_inbox", Integer.class));
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM pay_message_inbox WHERE consumer_key='pay.order.fence.v1'",
                        Integer.class));
        assertEquals(
                0,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM pay_message_inbox WHERE consumer_key='pay.order.closed.v1'",
                        Integer.class));
    }

    @Test
    void providerCloseFailureKeepsWaitingAndDoesNotCommitFence() {
        var inbox =
                proxy(
                        new MessageInboxGatewayImpl(session.getMapper(MessageInboxMapper.class)),
                        MessageInboxGateway.class);
        when(orders.findByOrderNo("O1"))
                .thenReturn(new OrderPaymentSnapshot("O1", 7L, new BigDecimal("10.00"), "CLOSED"));
        var channels = mock(PaymentChannelCloseGateway.class);
        when(channels.closeUnpaid(any())).thenReturn(false, true);
        var handler =
                proxy(
                        new OrderClosedEventHandler(
                                inbox, orders, payments, channels, new InboundEventGatewayImpl()),
                        OrderClosedEventHandler.class);
        var event =
                new CommerceEventEnvelope(
                        "CLOSE_FAIL",
                        "ORDER_CLOSED",
                        1,
                        "O1",
                        "O1",
                        Instant.now(),
                        null,
                        JsonUtils.toJsonStr(new OrderClosedEvent("O1", 7L, List.of())));
        assertThrows(IllegalStateException.class, () -> handler.execute(event));
        assertEquals("WAITING", payments.findByPaymentNo("P1").status());
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM pay_message_inbox", Integer.class));
        handler.execute(event);
        assertEquals("CLOSED", payments.findByPaymentNo("P1").status());
    }

    @Test
    void delayedPreparedSnapshotIsRecheckedAfterCloseCommits() throws Exception {
        var inbox =
                proxy(
                        new MessageInboxGatewayImpl(session.getMapper(MessageInboxMapper.class)),
                        MessageInboxGateway.class);
        when(orders.findByOrderNo("O2"))
                .thenReturn(new OrderPaymentSnapshot("O2", 7L, new BigDecimal("10.00"), "CLOSED"));
        var handler =
                proxy(
                        new OrderClosedEventHandler(
                                inbox, orders, payments, p -> true, new InboundEventGatewayImpl()),
                        OrderClosedEventHandler.class);
        var event =
                new CommerceEventEnvelope(
                        "CLOSE_EARLY",
                        "ORDER_CLOSED",
                        1,
                        "O2",
                        "O2",
                        Instant.now(),
                        null,
                        JsonUtils.toJsonStr(new OrderClosedEvent("O2", 7L, List.of())));
        var prepared = new CountDownLatch(1);
        var resume = new CountDownLatch(1);
        when(orders.preparePayment("O2"))
                .thenAnswer(
                        call -> {
                            prepared.countDown();
                            if (!resume.await(10, TimeUnit.SECONDS)) {
                                throw new IllegalStateException("测试未恢复原支付请求");
                            }
                            return new OrderPaymentSnapshot(
                                    "O2", 7L, new BigDecimal("10.00"), "PENDING_PAYMENT");
                        });
        var create =
                proxy(
                        new TradePaymentCreateCmdExe(orders, payments, inbox),
                        TradePaymentCreateCmdExe.class);
        var worker = Executors.newSingleThreadExecutor();
        try {
            var attempt = worker.submit(() -> create.execute("O2", "ALIPAY"));
            assertTrue(prepared.await(10, TimeUnit.SECONDS));
            handler.execute(event);
            resume.countDown();
            var error =
                    assertThrows(ExecutionException.class, () -> attempt.get(10, TimeUnit.SECONDS));
            assertInstanceOf(IllegalStateException.class, error.getCause());
        } finally {
            resume.countDown();
            worker.shutdownNow();
        }
        assertTrue(payments.findByOrderNo("O2").isEmpty());
        assertEquals(
                1,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM pay_message_inbox WHERE business_key='O2'",
                        Integer.class));
    }

    @Test
    void availableOrderCanCreateOnePaymentAndRejectsChangedPreparedAmount() {
        var inbox =
                proxy(
                        new MessageInboxGatewayImpl(session.getMapper(MessageInboxMapper.class)),
                        MessageInboxGateway.class);
        var create =
                proxy(
                        new TradePaymentCreateCmdExe(orders, payments, inbox),
                        TradePaymentCreateCmdExe.class);
        var pending =
                new OrderPaymentSnapshot("O3", 7L, new BigDecimal("10.00"), "PENDING_PAYMENT");
        when(orders.preparePayment("O3")).thenReturn(pending);
        when(orders.findByOrderNo("O3"))
                .thenReturn(
                        new OrderPaymentSnapshot(
                                "O3", 7L, new BigDecimal("11.00"), "PENDING_PAYMENT"));
        assertThrows(IllegalStateException.class, () -> create.execute("O3", "ALIPAY"));
        assertTrue(payments.findByOrderNo("O3").isEmpty());
        when(orders.findByOrderNo("O3")).thenReturn(pending);
        var payment = create.execute("O3", "ALIPAY");
        assertEquals(payment.paymentNo(), create.execute("O3", "ALIPAY").paymentNo());
        assertEquals(1, payments.findByOrderNo("O3").size());
    }

    @Test
    void historicalClosureStillRejectsChangedPayloadAndBlocksStaleCreation() {
        var inbox =
                proxy(
                        new MessageInboxGatewayImpl(session.getMapper(MessageInboxMapper.class)),
                        MessageInboxGateway.class);
        var handler =
                proxy(
                        new OrderClosedEventHandler(
                                inbox, orders, payments, p -> true, new InboundEventGatewayImpl()),
                        OrderClosedEventHandler.class);
        String closed = JsonUtils.toJsonStr(new OrderClosedEvent("OLD", 7L, List.of()));
        var original =
                new CommerceEventEnvelope(
                        "OLD-1", "ORDER_CLOSED", 1, "OLD", "OLD", Instant.now(), null, closed);
        transaction.executeWithoutResult(
                state ->
                        inbox.begin("pay.order.closed.v1", "OLD-1", "ORDER_CLOSED", "OLD", closed));
        handler.execute(original);
        var differentOwner =
                new CommerceEventEnvelope(
                        "OLD-2",
                        "ORDER_CLOSED",
                        1,
                        "OLD",
                        "OLD",
                        Instant.now(),
                        null,
                        JsonUtils.toJsonStr(new OrderClosedEvent("OLD", 8L, List.of())));
        assertThrows(IllegalStateException.class, () -> handler.execute(differentOwner));
        when(orders.preparePayment("OLD"))
                .thenReturn(
                        new OrderPaymentSnapshot(
                                "OLD", 7L, new BigDecimal("10.00"), "PENDING_PAYMENT"));
        var create =
                proxy(
                        new TradePaymentCreateCmdExe(orders, payments, inbox),
                        TradePaymentCreateCmdExe.class);
        assertThrows(IllegalStateException.class, () -> create.execute("OLD", "ALIPAY"));
        assertTrue(payments.findByOrderNo("OLD").isEmpty());
        verify(orders, never()).findByOrderNo("OLD");
    }
}
