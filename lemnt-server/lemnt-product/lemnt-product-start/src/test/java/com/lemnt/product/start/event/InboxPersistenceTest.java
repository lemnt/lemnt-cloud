package com.lemnt.product.start.event;

import com.lemnt.product.domain.event.gateway.MessageInboxGateway;
import com.lemnt.product.infrastructure.event.gateway.MessageInboxGatewayImpl;
import com.lemnt.product.infrastructure.event.mapper.MessageInboxMapper;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** 消费端幂等必须与业务同事务，并识别同号不同冻结载荷。 */
class InboxPersistenceTest extends OutboxPersistenceTest {
    MessageInboxGateway inbox() {
        return proxy(
                new MessageInboxGatewayImpl(session.getMapper(MessageInboxMapper.class)),
                MessageInboxGateway.class);
    }

    @Test
    void failedBusinessRollsBackReceiptAndAllowsOriginalEventToRetry() {
        var inbox = inbox();
        assertThrows(
                IllegalStateException.class,
                () ->
                        transaction.executeWithoutResult(
                                status -> {
                                    assertTrue(
                                            inbox.begin(
                                                    "stock.refund",
                                                    "E1",
                                                    "AFTER_SALE_COMPLETED",
                                                    "AF1",
                                                    "{}"));
                                    throw new IllegalStateException("模拟库存写入失败");
                                }));
        assertEquals(
                0,
                jdbc.queryForObject("SELECT COUNT(*) FROM product_message_inbox", Integer.class));
        transaction.executeWithoutResult(
                status ->
                        assertTrue(
                                inbox.begin(
                                        "stock.refund",
                                        "E1",
                                        "AFTER_SALE_COMPLETED",
                                        "AF1",
                                        "{}")));
        transaction.executeWithoutResult(
                status ->
                        assertFalse(
                                inbox.begin(
                                        "stock.refund",
                                        "E1",
                                        "AFTER_SALE_COMPLETED",
                                        "AF1",
                                        "{}")));
    }

    @Test
    void businessDuplicateIsDeduplicatedButChangedContentOrEventIdentityFails() {
        var inbox = inbox();
        transaction.executeWithoutResult(
                status ->
                        assertTrue(
                                inbox.begin(
                                        "stock.refund",
                                        "E1",
                                        "AFTER_SALE_COMPLETED",
                                        "AF1",
                                        "{}")));
        transaction.executeWithoutResult(
                status ->
                        assertFalse(
                                inbox.begin(
                                        "stock.refund",
                                        "E2",
                                        "AFTER_SALE_COMPLETED",
                                        "AF1",
                                        "{}")));
        assertThrows(
                IllegalStateException.class,
                () ->
                        transaction.executeWithoutResult(
                                status ->
                                        inbox.begin(
                                                "stock.refund",
                                                "E1",
                                                "AFTER_SALE_COMPLETED",
                                                "AF1",
                                                "{\"quantity\":2}")));
        assertThrows(
                IllegalStateException.class,
                () ->
                        transaction.executeWithoutResult(
                                status ->
                                        inbox.begin(
                                                "stock.refund", "E1", "ORDER_PAID", "O2", "{}")));
    }

    @Test
    void concurrentBusinessDuplicateOnlyAppliesOneEffect() throws Exception {
        var inbox = inbox();
        jdbc.execute("CREATE TABLE test_effect(id INT PRIMARY KEY)");
        var start = new java.util.concurrent.CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var first =
                    pool.submit(
                            () -> {
                                start.await();
                                transaction.executeWithoutResult(
                                        status -> {
                                            if (inbox.begin(
                                                    "stock.refund",
                                                    "E1",
                                                    "AFTER_SALE_COMPLETED",
                                                    "AF1",
                                                    "{}"))
                                                jdbc.update("INSERT INTO test_effect VALUES(1)");
                                        });
                                return true;
                            });
            var second =
                    pool.submit(
                            () -> {
                                start.await();
                                transaction.executeWithoutResult(
                                        status -> {
                                            if (inbox.begin(
                                                    "stock.refund",
                                                    "E2",
                                                    "AFTER_SALE_COMPLETED",
                                                    "AF1",
                                                    "{}"))
                                                jdbc.update("INSERT INTO test_effect VALUES(1)");
                                        });
                                return true;
                            });
            start.countDown();
            assertTrue(first.get(10, TimeUnit.SECONDS));
            assertTrue(second.get(10, TimeUnit.SECONDS));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM test_effect", Integer.class));
            assertEquals(
                    1,
                    jdbc.queryForObject(
                            "SELECT COUNT(*) FROM product_message_inbox", Integer.class));
        } finally {
            pool.shutdownNow();
        }
    }
}
