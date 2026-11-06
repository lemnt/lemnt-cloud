package com.lemnt.promotion.start.product;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.event.AfterSaleCompletedEvent;
import com.lemnt.promotion.application.event.service.AfterSaleCompletedEventHandler;
import com.lemnt.promotion.application.promotion.cmd.DiscountActivityCreateCmd;
import com.lemnt.promotion.application.promotion.cmd.PointActivityCreateCmd;
import com.lemnt.promotion.application.promotion.cmd.PointProductCmd;
import com.lemnt.promotion.application.promotion.service.ActivityManagementService;
import com.lemnt.promotion.application.promotion.service.PointActivityManagementService;
import com.lemnt.promotion.application.promotion.service.PromotionResourceService;
import com.lemnt.promotion.client.bo.PromotionLockBo;
import com.lemnt.promotion.domain.order.gateway.PromotionOrderGateway;
import com.lemnt.promotion.domain.order.model.PromotionOrderResources;
import com.lemnt.promotion.domain.promotion.model.DiscountProduct;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 商品与营销分库，验证RPC未知结果重试、关单围栏、消费回滚及本库并发约束。
 */
class PromotionResourcePersistenceTest {
    CatalogPersistenceTest fixture;
    PromotionOrderGateway orders;
    PointActivityManagementService point;
    PromotionResourceService resources;
    Long activityId;
    Long skuId;
    Long spuId;

    @BeforeEach
    void setup() throws Exception {
        fixture = new CatalogPersistenceTest();
        fixture.setup();
        var product = fixture.create("RESOURCE", fixture.draft("RESOURCE-SKU", 10));
        spuId = product.id();
        skuId = product.skus().get(0).id();
        point = fixture.components.get(PointActivityManagementService.class);
        activityId =
            point.create(
                new PointActivityCreateCmd(
                    spuId,
                    "资源重试",
                    1,
                    List.of(
                        new PointProductCmd(
                            skuId, 10, 100, new BigDecimal("1.00"), 10))));
        orders = mock(PromotionOrderGateway.class);
        fixture.components.register(PromotionOrderGateway.class, orders);
        resources = fixture.components.get(PromotionResourceService.class);
        state("RESERVING", 2);
    }

    @AfterEach
    void close() throws Exception {
        fixture.closeSchemaConnection();
    }

    void state(String state, int quantity) {
        when(orders.resources("O1"))
            .thenReturn(
                new PromotionOrderResources(
                    1L,
                    "O1",
                    7L,
                    null,
                    "POINT",
                    activityId,
                    null,
                    null,
                    List.of(new PromotionOrderResources.Line(spuId, skuId, quantity)),
                    state));
    }

    PromotionLockBo lock(int quantity) {
        return new PromotionLockBo(
            1L,
            "O1",
            7L,
            null,
            "POINT",
            activityId,
            null,
            null,
            List.of(new PromotionLockBo.Line(spuId, skuId, quantity)));
    }

    int stock() {
        return point.detail(activityId).products().get(0).stock();
    }

    CommerceEventEnvelope refund(String eventNo, String afterSaleNo, boolean full) {
        var event =
            new AfterSaleCompletedEvent(
                "O1",
                afterSaleNo,
                7L,
                10L,
                skuId,
                1,
                new BigDecimal("1.00"),
                50,
                0,
                1,
                full);
        return new CommerceEventEnvelope(
            eventNo,
            "AFTER_SALE_COMPLETED",
            1,
            afterSaleNo,
            "O1",
            Instant.parse("2026-11-03T00:00:00Z"),
            null,
            JsonUtils.toJsonStr(event));
    }

    @Test
    void duplicateReserveAndPaidDoNotRepeatStockAndRefundEventsApplyOnce() {
        assertEquals("LOCKED", resources.lock(lock(2)).status());
        assertEquals("LOCKED", resources.lock(lock(2)).status());
        assertEquals(8, stock());
        state("PAID", 2);
        resources.confirm("O1");
        resources.confirm("O1");
        assertEquals(8, stock());
        var handler = fixture.components.get(AfterSaleCompletedEventHandler.class);
        var first = refund("E1", "AF1", false);
        handler.execute(first);
        handler.execute(first);
        handler.execute(refund("E2", "AF1", false));
        assertEquals(9, stock());
        assertTrue(resources.refundApplied("AF1"));
        state("REFUNDED", 2);
        handler.execute(refund("E3", "AF2", true));
        assertEquals(10, stock());
        assertTrue(resources.refundApplied("AF2"));
    }

    @Test
    void closeTombstoneFencesLateReserveAndDoesNotRestoreUnreservedStock() {
        state("CLOSED", 2);
        resources.rollback("O1");
        resources.rollback("O1");
        state("RESERVING", 2);
        assertEquals("RELEASED", resources.lock(lock(2)).status());
        assertEquals(10, stock());
    }

    @Test
    void changedResourceFingerprintIsRejectedWithoutChangingStock() {
        resources.lock(lock(2));
        assertThrows(IllegalStateException.class, () -> resources.lock(lock(3)));
        state("RESERVING", 3);
        assertThrows(IllegalStateException.class, () -> resources.lock(lock(3)));
        assertEquals(8, stock());
    }

    @Test
    void missingPaidPredecessorRollsBackRefundReceiptAndOriginalEventCanRetry() {
        resources.lock(lock(2));
        var handler = fixture.components.get(AfterSaleCompletedEventHandler.class);
        var event = refund("E1", "AF1", false);
        assertThrows(IllegalStateException.class, () -> handler.execute(event));
        assertFalse(resources.refundApplied("AF1"));
        assertEquals(8, stock());
        state("PAID", 2);
        resources.confirm("O1");
        handler.execute(event);
        assertEquals(9, stock());
    }

    @Test
    void concurrentIdenticalLocksOnlyReserveOnce() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            var first =
                pool.submit(
                    () -> {
                        start.await();
                        return resources.lock(lock(2)).status();
                    });
            var second =
                pool.submit(
                    () -> {
                        start.await();
                        return resources.lock(lock(2)).status();
                    });
            start.countDown();
            assertEquals("LOCKED", first.get(10, TimeUnit.SECONDS));
            assertEquals("LOCKED", second.get(10, TimeUnit.SECONDS));
            assertEquals(8, stock());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void marketingMutexPreventsTwoEnabledDiscountsWithoutProductRowLocks() throws Exception {
        var service = fixture.components.get(ActivityManagementService.class);
        var now = LocalDateTime.now();
        var cmd =
            new DiscountActivityCreateCmd(
                "并发折扣",
                now.minusDays(1),
                now.plusDays(1),
                null,
                List.of(
                    new DiscountProduct(
                        null, null, spuId, skuId, "PERCENT", 8000, null)));
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        Callable<Boolean> task =
            () -> {
                start.await();
                try {
                    service.createDiscount(cmd);
                    return true;
                } catch (ServiceException expected) {
                    return false;
                }
            };
        try {
            var first = pool.submit(task);
            var second = pool.submit(task);
            start.countDown();
            assertNotEquals(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
            assertEquals(1, fixture.count("promotion_discount_activity"));
        } finally {
            pool.shutdownNow();
        }
    }
}
