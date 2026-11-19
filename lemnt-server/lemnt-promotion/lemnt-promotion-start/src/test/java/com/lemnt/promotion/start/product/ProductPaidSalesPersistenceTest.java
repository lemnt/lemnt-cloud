package com.lemnt.promotion.start.product;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.dto.OrderStockLineDTO;
import com.lemnt.order.client.event.AfterSaleCompletedEvent;
import com.lemnt.order.client.event.OrderPaidEvent;
import com.lemnt.product.application.event.service.OrderStockEventHandler;
import com.lemnt.product.domain.event.gateway.MessageInboxGateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** 按真实Mapper和事务验证已确认库存、销量事实与历史消费凭证的边界。 */
class ProductPaidSalesPersistenceTest {
    private final ProductDatabaseFixture fixture = new ProductDatabaseFixture();
    private OrderStockEventHandler handler;
    private Long spu;
    private Long first;
    private Long second;

    @BeforeEach
    void setup() throws Exception {
        fixture.setup();
        handler = fixture.components.get(OrderStockEventHandler.class);
        var product = fixture.create("SALES", fixture.draft("ONE", 10), fixture.draft("TWO", 10));
        spu = product.id();
        fixture.shelf(spu, true);
        first = product.skus().get(0).id();
        second = product.skus().get(1).id();
    }

    @AfterEach
    void close() throws Exception {
        fixture.schema.close();
    }

    @Test
    void localReserveCommitsWithItsLedgerAndRemainsIdempotentAcrossCallerRollback() {
        var transactions =
                new TransactionTemplate(new DataSourceTransactionManager(fixture.source));
        transactions.executeWithoutResult(
                status -> {
                    assertTrue(fixture.inventory.reserveStock("ORDER", first, 2));
                    status.setRollbackOnly();
                });
        assertTrue(fixture.inventory.reserveStock("ORDER", first, 2));
        assertThrows(
                IllegalStateException.class,
                () -> fixture.inventory.reserveStock("ORDER", first, 3));
        assertEquals(8, fixture.skus.catalogSku(first).stock());
        fixture.inventory.cancelReservation("ORDER", first, 2);
        assertFalse(fixture.inventory.reserveStock("ORDER", first, 2));
        assertEquals(10, fixture.skus.catalogSku(first).stock());
    }

    @Test
    void failedReservationInsertRollsBackStockAndEarlyCancellationPreventsSubsequentDeduction() {
        var jdbc = new JdbcTemplate(fixture.source);
        jdbc.execute(
                "ALTER TABLE product_stock_reservation ADD CONSTRAINT reject_failed_reservation CHECK (reservation_no <> 'FAILED')");
        assertThrows(
                RuntimeException.class, () -> fixture.inventory.reserveStock("FAILED", first, 2));
        assertNull(fixture.inventory.reservationStatus("FAILED", first));
        assertEquals(10, fixture.skus.catalogSku(first).stock());
        fixture.inventory.cancelReservation("CLOSED", first, 2);
        assertFalse(fixture.inventory.reserveStock("CLOSED", first, 2));
        assertEquals(10, fixture.skus.catalogSku(first).stock());
    }

    @Test
    void directInventoryConfirmationDoesNotImplySalesAndSaleEnablesWaitingRefund() {
        assertTrue(fixture.inventory.reserveStock("ORDER", first, 2));
        fixture.inventory.confirmStock("ORDER", first);
        var refund =
                event(
                        "REFUND",
                        CommerceEventTopics.AFTER_SALE_COMPLETED,
                        "AFTER",
                        new AfterSaleCompletedEvent(
                                "ORDER",
                                "AFTER",
                                1L,
                                1L,
                                first,
                                1,
                                BigDecimal.ONE,
                                0,
                                0,
                                1,
                                false));
        assertThrows(IllegalStateException.class, () -> handler.refunded(refund));
        assertEquals(0, inboxRows());

        handler.paid(paid("E1", new OrderStockLineDTO(first, 2)));
        handler.paid(paid("E2", new OrderStockLineDTO(first, 2)));
        assertThrows(
                IllegalStateException.class,
                () -> handler.paid(paid("CHANGED", new OrderStockLineDTO(first, 1))));
        assertEquals(
                Boolean.TRUE, fixture.inventory.reservationStatus("ORDER", first).salesRecorded());
        assertEquals(2, fixture.spus.salesCount(spu));
        assertEquals(2L, fixture.skus.catalogSku(first).salesCount());
        assertEquals(0, inboxRows());
        handler.refunded(refund);
        assertEquals(9, fixture.skus.catalogSku(first).stock());
    }

    @Test
    void failedSecondLineRollsBackFirstLineStockAndSales() {
        assertTrue(fixture.inventory.reserveStock("ORDER", first, 2));
        var paid = paid("E1", new OrderStockLineDTO(first, 2), new OrderStockLineDTO(second, 3));
        assertThrows(IllegalStateException.class, () -> handler.paid(paid));
        var reservation = fixture.inventory.reservationStatus("ORDER", first);
        assertEquals("RESERVED", reservation.status());
        assertNull(reservation.salesRecorded());
        assertEquals(0L, fixture.skus.catalogSku(first).salesCount());
        assertEquals(0, fixture.spus.salesCount(spu));

        assertTrue(fixture.inventory.reserveStock("ORDER", second, 3));
        handler.paid(paid);
        assertEquals(5, fixture.spus.salesCount(spu));
        assertEquals(0, inboxRows());
    }

    @Test
    void concurrentDeliveriesRecordEachSkuAndSpuSalesOnlyOnce() throws Exception {
        assertTrue(fixture.inventory.reserveStock("ORDER", first, 2));
        assertTrue(fixture.inventory.reserveStock("ORDER", second, 3));
        var line1 = new OrderStockLineDTO(first, 2);
        var line2 = new OrderStockLineDTO(second, 3);
        var start = new CountDownLatch(1);
        var workers = Executors.newFixedThreadPool(2);
        try {
            var firstDelivery =
                    workers.submit(
                            () -> {
                                start.await();
                                handler.paid(paid("E1", line1, line2));
                                return null;
                            });
            var secondDelivery =
                    workers.submit(
                            () -> {
                                start.await();
                                handler.paid(paid("E2", line2, line1));
                                return null;
                            });
            start.countDown();
            firstDelivery.get(10, TimeUnit.SECONDS);
            secondDelivery.get(10, TimeUnit.SECONDS);
            assertEquals(2L, fixture.skus.catalogSku(first).salesCount());
            assertEquals(3L, fixture.skus.catalogSku(second).salesCount());
            assertEquals(5, fixture.spus.salesCount(spu));
            assertEquals(0, inboxRows());
        } finally {
            workers.shutdownNow();
        }
    }

    @Test
    void historicalConsumptionIsVerifiedWithoutRecordingSalesAgain() {
        assertTrue(fixture.inventory.reserveStock("ORDER", first, 2));
        fixture.inventory.confirmStock("ORDER", first);
        var paid = paid("E1", new OrderStockLineDTO(first, 2));
        var transactions =
                new TransactionTemplate(new DataSourceTransactionManager(fixture.source));
        transactions.executeWithoutResult(
                status -> {
                    fixture.skus.addSales(first, 2);
                    fixture.spus.addSales(spu, 2);
                    fixture.components
                            .get(MessageInboxGateway.class)
                            .begin(
                                    "product.order_paid.v1",
                                    paid.eventNo(),
                                    paid.eventType(),
                                    paid.businessKey(),
                                    paid.payload());
                });
        handler.paid(paid("E2", new OrderStockLineDTO(first, 2)));
        assertThrows(
                IllegalStateException.class,
                () -> handler.paid(paid("CHANGED", new OrderStockLineDTO(first, 1))));
        assertNull(fixture.inventory.reservationStatus("ORDER", first).salesRecorded());
        assertEquals(2, fixture.spus.salesCount(spu));
        assertEquals(1, inboxRows());
    }

    private CommerceEventEnvelope paid(String eventNo, OrderStockLineDTO... lines) {
        return event(
                eventNo,
                CommerceEventTopics.ORDER_PAID,
                "ORDER",
                new OrderPaidEvent("ORDER", 1L, BigDecimal.ONE, 0, 0, Map.of(), List.of(lines)));
    }

    private CommerceEventEnvelope event(String id, String type, String key, Object data) {
        return new CommerceEventEnvelope(
                id, type, 1, key, "ORDER", Instant.now(), null, JsonUtils.toJsonStr(data));
    }

    private int inboxRows() {
        return new JdbcTemplate(fixture.source)
                .queryForObject("SELECT COUNT(*) FROM product_message_inbox", Integer.class);
    }
}
