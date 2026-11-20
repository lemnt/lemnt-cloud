package com.lemnt.pay.start.event;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.event.OrderClosedEvent;
import com.lemnt.pay.application.event.service.OrderClosedEventHandler;
import com.lemnt.pay.application.payment.executor.TradePaymentCreateCmdExe;
import com.lemnt.pay.application.payment.service.PayNotifyDispatcher;
import com.lemnt.pay.application.payment.service.PayNotifyTaskService;
import com.lemnt.pay.application.payment.service.PaymentAttemptService;
import com.lemnt.pay.application.payment.service.PaymentNotificationService;
import com.lemnt.pay.application.payment.service.PaymentRecoveryService;
import com.lemnt.pay.application.payment.service.RechargeService;
import com.lemnt.pay.application.payment.service.WalletService;
import com.lemnt.pay.application.paymentrefund.service.PaymentRefundService;
import com.lemnt.pay.domain.channel.gateway.PayAppGateway;
import com.lemnt.pay.domain.channel.gateway.PaymentChannelCloseGateway;
import com.lemnt.pay.domain.channel.gateway.PaymentChannelGateway;
import com.lemnt.pay.domain.channel.model.PayApp;
import com.lemnt.pay.domain.channel.model.PayChannelConfig;
import com.lemnt.pay.domain.channel.model.PaymentChannelNotification;
import com.lemnt.pay.domain.event.gateway.MessageInboxGateway;
import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.order.model.FrozenRefundPlan;
import com.lemnt.pay.domain.order.model.OrderPaymentSnapshot;
import com.lemnt.pay.domain.payment.gateway.PayAppNotifyGateway;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PaymentOperator;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundChannelGateway;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;
import com.lemnt.pay.infrastructure.channel.support.PayChannelConfigResolver;
import com.lemnt.pay.infrastructure.event.gateway.InboundEventGatewayImpl;
import com.lemnt.pay.infrastructure.event.gateway.MessageInboxGatewayImpl;
import com.lemnt.pay.infrastructure.event.mapper.MessageInboxMapper;
import com.lemnt.pay.infrastructure.payment.gateway.PayNotifyTaskGatewayImpl;
import com.lemnt.pay.infrastructure.payment.gateway.TradePaymentGatewayImpl;
import com.lemnt.pay.infrastructure.payment.mapper.PayNotifyLogMapper;
import com.lemnt.pay.infrastructure.payment.mapper.PayNotifyTaskMapper;
import com.lemnt.pay.infrastructure.payment.mapper.PaymentOrderMapper;
import com.lemnt.pay.infrastructure.payment.mapper.TradePaymentMapper;
import com.lemnt.pay.infrastructure.paymentrefund.gateway.PaymentRefundGatewayImpl;
import com.lemnt.pay.infrastructure.paymentrefund.mapper.PaymentRefundMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 支付库没有任何订单/售后表，验证资金和事件原子性及退款额度。
 */
class CashPaymentPersistenceTest extends PayDatabaseFixture {
    OrderPaymentGateway orders;
    TradePaymentGateway payments;
    PaymentRefundGateway refunds;
    PaymentNotificationService notifications;
    PaymentRefundService refundService;
    PayNotifyTaskService tasks;
    PayChannelConfigResolver channelConfigs;
    PayAppGateway apps;
    PayAppNotifyGateway appNotify;

    @BeforeEach
    void cashSetup() {
        orders = mock(OrderPaymentGateway.class);
        when(orders.findByOrderNo("O1"))
            .thenReturn(
                new OrderPaymentSnapshot(
                    "O1", 7L, new BigDecimal("10.00"), "PENDING_PAYMENT"));
        channelConfigs = mock(PayChannelConfigResolver.class);
        apps = mock(PayAppGateway.class);
        appNotify = mock(PayAppNotifyGateway.class);
        payments =
                proxy(
                        new TradePaymentGatewayImpl(
                                session.getMapper(TradePaymentMapper.class),
                                session.getMapper(PaymentOrderMapper.class),
                                channelConfigs,
                                apps),
                        TradePaymentGateway.class);
        refunds =
                proxy(
                        new PaymentRefundGatewayImpl(
                                session.getMapper(PaymentRefundMapper.class), payments, apps),
                        PaymentRefundGateway.class);
        tasks =
                new PayNotifyTaskService(
                        new PayNotifyTaskGatewayImpl(
                                session.getMapper(PayNotifyTaskMapper.class),
                                session.getMapper(PayNotifyLogMapper.class)),
                        new PayNotifyDispatcher(payments, refunds, orders, appNotify),
                        event -> {});
        notifications =
                proxy(
                        new PaymentNotificationService(
                                orders, payments, tasks, mock(RechargeService.class)),
                        PaymentNotificationService.class);
        refundService =
                proxy(
                        new PaymentRefundService(
                                orders,
                                payments,
                                refunds,
                                tasks,
                                mock(WalletService.class),
                                mock(RechargeService.class)),
                        PaymentRefundService.class);
        payments.createWaiting("P1", "O1", 7L, "SANDBOX", "app", new BigDecimal("10.00"));
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
    void callbackWritesOnlyPaymentAndOneNotificationTaskAndRejectsDifferentChannelReplay() {
        notifications.execute(notice("CH1"));
        notifications.execute(notice("CH1"));
        assertEquals("SUCCESS", payments.findByPaymentNo("P1").status());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM pay_notify_task", Integer.class));
        assertThrows(IllegalStateException.class, () -> notifications.execute(notice("CH2")));
        assertEquals("CH1", payments.findByPaymentNo("P1").channelTradeNo());
        assertEquals(
            0,
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('ORDER_INFO','ORDER_AFTER_SALE','TRADE_ORDER')",
                Integer.class));
    }

    @Test
    void successfulPaymentKeepsFeeSnapshotAfterConfigChanges() {
        jdbc.update("UPDATE pay_payment SET channel='WECHAT' WHERE payment_no='P1'");
        when(channelConfigs.resolve("app", "WECHAT"))
                .thenReturn(
                        new PayChannelConfig(
                                1L, 1L, "WECHAT", 0, new BigDecimal("2.55"), null, null, null));
        var notice =
                new VerifiedPaymentNotification(
                        "P1", "O1", "WECHAT", "FEE-PAY", new BigDecimal("10.00"), "FEE-NOTICE");
        notifications.execute(notice);
        when(channelConfigs.resolve("app", "WECHAT"))
                .thenReturn(
                        new PayChannelConfig(
                                1L, 1L, "WECHAT", 0, new BigDecimal("8.00"), null, null, null));
        notifications.execute(notice);
        var saved = payments.findByPaymentNo("P1");
        assertEquals(new BigDecimal("2.55"), saved.channelFeeRate());
        assertEquals(new BigDecimal("0.26"), saved.channelFeeAmount());
        verify(channelConfigs).resolve("app", "WECHAT");
    }

    @Test
    void failedTaskRegistrationRollsBackPaymentAndCanRetry() {
        jdbc.execute(
                "ALTER TABLE pay_notify_task ADD CONSTRAINT test_reject_pay CHECK (type <> 'PAY')");
        assertThrows(RuntimeException.class, () -> notifications.execute(notice("CH1")));
        assertEquals("WAITING", payments.findByPaymentNo("P1").status());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM pay_notify_task", Integer.class));
        jdbc.execute("ALTER TABLE pay_notify_task DROP CONSTRAINT test_reject_pay");
        notifications.execute(notice("CH1"));
        assertEquals("SUCCESS", payments.findByPaymentNo("P1").status());
    }

    @Test
    void notificationIsRetriedUntilOrderAcknowledgesAndThenStops() {
        notifications.execute(notice("CH1"));
        Long id = jdbc.queryForObject("SELECT id FROM pay_notify_task", Long.class);
        doThrow(new IllegalStateException("订单服务暂不可用"))
                .doNothing()
                .when(orders)
                .notifyPaid("O1", "P1");
        assertFalse(tasks.attempt(id));
        assertEquals(
                "FAILED", jdbc.queryForObject("SELECT status FROM pay_notify_task", String.class));
        jdbc.update(
                "UPDATE pay_notify_task SET next_notify_time=TIMESTAMPADD(SECOND,-1,CURRENT_TIMESTAMP)");
        assertEquals(1, tasks.processDue(10));
        assertTrue(tasks.attempt(id));
        verify(orders, times(2)).notifyPaid("O1", "P1");
        assertEquals(
                "SUCCESS", jdbc.queryForObject("SELECT status FROM pay_notify_task", String.class));
    }

    @EnableAsync
    @EnableTransactionManagement
    static class AsyncConfiguration {}

    @Test
    void asyncDispatchIsQueuedOnlyAfterTheMoneyTransactionCommits() {
        String url = "https://business.example.test/pay";
        when(apps.findByAppKey("app"))
                .thenReturn(new PayApp(1L, "app", "商城", 0, null, url, null, null));
        when(orders.findByOrderNo("O2"))
                .thenReturn(
                        new OrderPaymentSnapshot(
                                "O2", 7L, new BigDecimal("10.00"), "PENDING_PAYMENT"));
        payments.createWaiting("P2", "O2", 7L, "SANDBOX", "app", new BigDecimal("10.00"));
        assertEquals(url, payments.findByPaymentNo("P2").notifyUrl());
        when(apps.findByAppKey("app"))
                .thenReturn(new PayApp(1L, "app", "商城", 0, null, null, null, null));
        var notice =
                new VerifiedPaymentNotification(
                        "P2", "O2", "SANDBOX", "CH2", new BigDecimal("10.00"), "NOTICE2");
        Map<String, String> payload =
                Map.of("orderNo", "O2", "paymentNo", "P2", "status", "SUCCESS");
        doThrow(new IllegalStateException("接收端未确认应用通知"))
                .doNothing()
                .when(appNotify)
                .post(url, payload);
        var work = new ArrayDeque<Runnable>();
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(AsyncConfiguration.class);
            context.registerBean("taskExecutor", Executor.class, () -> work::add);
            context.registerBean(
                    PayNotifyTaskService.class,
                    () ->
                            new PayNotifyTaskService(
                                    new PayNotifyTaskGatewayImpl(
                                            session.getMapper(PayNotifyTaskMapper.class),
                                            session.getMapper(PayNotifyLogMapper.class)),
                                    new PayNotifyDispatcher(payments, refunds, orders, appNotify),
                                    context));
            context.refresh();
            var service =
                    new PaymentNotificationService(
                            orders,
                            payments,
                            context.getBean(PayNotifyTaskService.class),
                            mock(RechargeService.class));
            transaction.executeWithoutResult(
                    status -> {
                        service.execute(notice);
                        assertTrue(work.isEmpty());
                        status.setRollbackOnly();
                    });
            assertTrue(work.isEmpty());
            assertEquals("WAITING", payments.findByPaymentNo("P2").status());
            assertEquals(
                    0, jdbc.queryForObject("SELECT COUNT(*) FROM pay_notify_task", Integer.class));
            transaction.executeWithoutResult(
                    status -> {
                        service.execute(notice);
                        assertTrue(work.isEmpty());
                    });
            assertEquals(2, work.size());
            verify(orders, never()).notifyPaid("O2", "P2");
            verifyNoInteractions(appNotify);
            work.remove().run();
            work.remove().run();
            verify(orders).notifyPaid("O2", "P2");
            assertEquals(
                    "SUCCESS",
                    jdbc.queryForObject(
                            "SELECT status FROM pay_notify_task WHERE type='PAY'", String.class));
            Long appTask =
                    jdbc.queryForObject(
                            "SELECT id FROM pay_notify_task WHERE type='PAY_HTTP'", Long.class);
            assertEquals(
                    "FAILED",
                    jdbc.queryForObject(
                            "SELECT status FROM pay_notify_task WHERE id=?",
                            String.class,
                            appTask));
            assertTrue(tasks.retry(appTask));
            verify(appNotify, times(2)).post(url, payload);
            verifyNoMoreInteractions(appNotify);
        }
    }

    @Test
    void refundAppKeepsOriginalAddressAndDeliversEachTerminalResultOnce() {
        notifications.execute(notice("CH1"));
        plan("A1", "2.00", "REFUNDING");
        String url = "https://business.example.test/refund";
        when(apps.findByAppKey("app"))
                .thenReturn(new PayApp(1L, "app", "商城", 0, null, null, url, null));
        var refund = refundService.prepare("A1");
        assertEquals(url, refunds.find(refund.refundNo()).notifyUrl());
        when(apps.findByAppKey("app")).thenReturn(null);
        var failed =
                new PaymentRefundResult(
                        refund.refundNo(),
                        refund.paymentNo(),
                        refund.channel(),
                        refund.amount(),
                        "FAILED",
                        null);
        refundService.complete(refund, failed);
        refundService.complete(refund, failed);
        assertEquals(
                List.of("PAY", "REFUND_FAIL_HTTP"),
                jdbc.queryForList("SELECT type FROM pay_notify_task ORDER BY id", String.class));
        var retry = refundService.prepare("A1");
        var success =
                new PaymentRefundResult(
                        retry.refundNo(),
                        retry.paymentNo(),
                        retry.channel(),
                        retry.amount(),
                        "SUCCESS",
                        "REF-1");
        refundService.complete(retry, success);
        refundService.complete(retry, success);
        assertEquals(4, tasks.processDue(10));
        verify(orders).notifyRefunded("O1", "A1", refund.refundNo());
        for (String status : List.of("FAILED", "SUCCESS")) {
            verify(appNotify)
                    .post(
                            url,
                            Map.of(
                                    "orderNo",
                                    "O1",
                                    "paymentNo",
                                    "P1",
                                    "afterSaleNo",
                                    "A1",
                                    "refundNo",
                                    refund.refundNo(),
                                    "status",
                                    status));
        }
        verifyNoMoreInteractions(appNotify);
    }

    /** 模拟回调丢失，查单和后来到达的回调共用同一支付任务。 */
    @Test
    void paymentQueryRecoversMissingCallbackAndReplayKeepsOneTask() {
        jdbc.update("UPDATE pay_payment SET channel='WECHAT' WHERE payment_no='P1'");
        var channels = mock(PaymentChannelGateway.class);
        var recovery =
                new PaymentRecoveryService(
                        payments,
                        refunds,
                        channels,
                        mock(PaymentRefundChannelGateway.class),
                        notifications,
                        refundService,
                        mock(PaymentAttemptService.class));
        var result = new VerifiedPaymentNotification(
            "P1", "O1", "WECHAT", "CH1", new BigDecimal("10.00"), "CH1");
        when(channels.query(any())).thenReturn(null, result);
        recovery.syncPayments();
        assertEquals("WAITING", payments.findByPaymentNo("P1").status());
        recovery.syncPayments();
        notifications.execute(result);
        recovery.syncPayments();
        verify(channels, times(2)).query(any());
        assertEquals("SUCCESS", payments.findByPaymentNo("P1").status());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM pay_notify_task WHERE type='PAY'", Integer.class));
    }

    /** 定时查询只读取原单，回调校验原应用并复用已建立的退款任务。 */
    /** 到期查单失败和渠道关闭失败都保留原尝试，关闭确认后才分配新单号。 */
    @Test
    void expiredWechatAttemptCanRetryOnlyAfterConfirmedClose() {
        var inbox =
                proxy(
                        new MessageInboxGatewayImpl(session.getMapper(MessageInboxMapper.class)),
                        MessageInboxGateway.class);
        var create =
                proxy(
                        new TradePaymentCreateCmdExe(orders, payments, inbox),
                        TradePaymentCreateCmdExe.class);
        when(orders.preparePayment("O1"))
                .thenReturn(
                        new OrderPaymentSnapshot(
                                "O1", 7L, new BigDecimal("10.00"), "PENDING_PAYMENT"));
        var first = create.execute("O1", "WECHAT", "app");
        var master = payments.findPaymentOrder("O1");
        assertEquals("WAITING", master.status());
        assertTrue(first.expiresAtEpochSecond() > payments.databaseEpochSecond());
        jdbc.update(
                "UPDATE pay_payment SET expires_at_epoch_second=1 WHERE payment_no=?",
                first.paymentNo());
        var channels = mock(PaymentChannelGateway.class);
        var close = mock(PaymentChannelCloseGateway.class);
        var attempts =
                proxy(
                        new PaymentAttemptService(payments, channels, close),
                        PaymentAttemptService.class);
        var recovery =
                new PaymentRecoveryService(
                        payments,
                        refunds,
                        channels,
                        mock(PaymentRefundChannelGateway.class),
                        notifications,
                        refundService,
                        attempts);
        when(channels.query(any())).thenThrow(new IllegalStateException("查询超时")).thenReturn(null);
        when(close.closeUnpaid(any())).thenReturn(false, true);
        recovery.expirePayments();
        verifyNoInteractions(close);
        assertEquals(first.paymentNo(), create.execute("O1", "WECHAT", "app").paymentNo());
        recovery.expirePayments();
        assertEquals("WAITING", payments.findByPaymentNo(first.paymentNo()).status());
        assertThrows(ServiceException.class, () -> attempts.prepay(first, null, null));
        recovery.expirePayments();
        assertEquals("CLOSED", payments.findByPaymentNo(first.paymentNo()).status());
        var second = create.execute("O1", "WECHAT", "app");
        assertNotEquals(first.paymentNo(), second.paymentNo());
        assertEquals(master, payments.findPaymentOrder("O1"));
        assertEquals(second.paymentNo(), create.execute("O1", "WECHAT", "app").paymentNo());
        assertEquals(
                2,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM pay_payment WHERE order_no='O1' AND channel='WECHAT'",
                        Integer.class));
    }

    /** 已收款的到期尝试补记资金状态，跳过渠道关单。 */
    @Test
    void expiredWechatAttemptWithFundsReceivedIsConfirmedInsteadOfClosed() {
        var first =
                payments.createWaiting("P2", "O1", 7L, "WECHAT", "app", new BigDecimal("10.00"));
        jdbc.update(
                "UPDATE pay_payment SET expires_at_epoch_second=1 WHERE payment_no=?",
                first.paymentNo());
        var channels = mock(PaymentChannelGateway.class);
        when(channels.query(any()))
                .thenReturn(
                        new VerifiedPaymentNotification(
                                "P2", "O1", "WECHAT", "TX-P2", new BigDecimal("10.00"), "TX-P2"));
        var attempts = mock(PaymentAttemptService.class);
        var recovery =
                new PaymentRecoveryService(
                        payments,
                        refunds,
                        channels,
                        mock(PaymentRefundChannelGateway.class),
                        notifications,
                        refundService,
                        attempts);
        recovery.expirePayments();
        assertEquals("SUCCESS", payments.findByPaymentNo("P2").status());
        verifyNoInteractions(attempts);
        assertEquals("SUCCESS", payments.findPaymentOrder("O1").status());
        assertEquals("P2", payments.findPaymentOrder("O1").paidPaymentNo());
    }

    /** 真实第二笔收款须入库，同时主单保留原成功引用并进入待对账。 */
    @Test
    void secondVerifiedPaymentLeavesBothFundsAndFlagsMasterForReconciliation() {
        var second =
                payments.createWaiting("P2", "O1", 7L, "WECHAT", "app", new BigDecimal("10.00"));
        var masterNo = payments.findPaymentOrder("O1").paymentOrderNo();
        notifications.execute(notice("ORIGINAL"));
        notifications.execute(
                new VerifiedPaymentNotification(
                        second.paymentNo(),
                        "O1",
                        "WECHAT",
                        "CHANNEL-SECOND",
                        new BigDecimal("10.00"),
                        "CHANNEL-SECOND"));
        assertEquals("SUCCESS", payments.findByPaymentNo("P1").status());
        assertEquals("SUCCESS", payments.findByPaymentNo("P2").status());
        var master = payments.findPaymentOrder("O1");
        assertEquals(masterNo, master.paymentOrderNo());
        assertEquals("P1", master.paidPaymentNo());
        assertEquals("RECONCILE_REQUIRED", master.status());
        tasks.processDue(10);
        verify(orders, never()).notifyPaid(any(), any());
        assertThrows(
                IllegalStateException.class,
                () ->
                        payments.createWaiting(
                                "P3", "O1", 7L, "WECHAT", "app", new BigDecimal("10.00")));
    }

    @Test
    void refundQueryAndCallbackRecoverWithoutResubmission() {
        jdbc.update("UPDATE pay_payment SET channel='WECHAT' WHERE payment_no='P1'");
        notifications.execute(new VerifiedPaymentNotification(
            "P1", "O1", "WECHAT", "CH1", new BigDecimal("10.00"), "CH1"));
        plan("AF1", "3.32", "REFUNDING");
        var refund = refundService.prepare("AF1");
        var channels = mock(PaymentRefundChannelGateway.class);
        var recovery =
                new PaymentRecoveryService(
                        payments,
                        refunds,
                        mock(PaymentChannelGateway.class),
                        channels,
                        notifications,
                        refundService,
                        mock(PaymentAttemptService.class));
        var result = new PaymentRefundResult(
            refund.refundNo(), "P1", "WECHAT", new BigDecimal("3.32"), "SUCCESS", "RCH1");
        when(channels.query(any(), any())).thenReturn(null, result);
        recovery.syncRefunds();
        assertEquals("WAITING", refunds.find(refund.refundNo()).status());
        var callback = new PaymentChannelNotification("body", "signature", "timestamp", "nonce", "serial", null);
        when(channels.verify("other", "WECHAT", callback)).thenReturn(result);
        assertThrows(IllegalStateException.class, () -> recovery.receiveRefund("other", "WECHAT", callback));
        assertEquals("WAITING", refunds.find(refund.refundNo()).status());
        recovery.syncRefunds();
        when(channels.verify("app", "WECHAT", callback)).thenReturn(result);
        recovery.receiveRefund("app", "wechat", callback);
        verify(channels, never()).refund(any(), any());
        assertEquals("SUCCESS", refunds.find(refund.refundNo()).status());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM pay_notify_task WHERE type='REFUND'", Integer.class));
    }

    @Test
    void genuineLatePaymentAfterOrderCloseIsRetainedForReconciliation() {
        payments.markClosed("P1");
        when(orders.findByOrderNo("O1"))
            .thenReturn(new OrderPaymentSnapshot("O1", 7L, new BigDecimal("10.00"), "CLOSED"));
        notifications.execute(notice("CH1"));
        assertEquals("SUCCESS", payments.findByPaymentNo("P1").status());
        assertEquals("RECONCILE_REQUIRED", payments.findPaymentOrder("O1").status());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM pay_notify_task", Integer.class));
    }

    @Test
    void frozenRefundIsIdempotentAndCashCompletionRegistersOneTask() {
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
                        "SELECT COUNT(*) FROM pay_notify_task WHERE type='REFUND'", Integer.class));
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
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM pay_notify_task", Integer.class));
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
