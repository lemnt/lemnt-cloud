package com.lemnt.product.start.product;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.dto.OrderStockLineDTO;
import com.lemnt.order.client.event.OrderPaidEvent;
import com.lemnt.product.application.event.service.OrderStockEventHandler;
import com.lemnt.product.application.product.executor.StoreProductDetailQueryExe;
import com.lemnt.product.application.product.executor.StoreProductPageQueryExe;
import com.lemnt.product.application.product.qry.StorePageQry;
import com.lemnt.product.domain.product.model.ProductSpu;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用生产DDL、Mapper和事务代理验证销量累计、事件幂等及公开查询，不连接真实MQ。
 */
class ProductSalesCountPersistenceTest {
    private CatalogPersistenceTest fixture;
    private OrderStockEventHandler handler;

    @BeforeEach
    void setup() throws Exception {
        fixture = new CatalogPersistenceTest();
        fixture.setup();
        handler = fixture.components.get(OrderStockEventHandler.class);
    }

    @AfterEach
    void close() throws Exception {
        fixture.closeSchemaConnection();
    }

    @Test
    void paid_whenSameSpuHasMultipleSkus_shouldSumOnceAndExposeSalesInQueries() {
        var spu = createOnShelf("MULTI");
        Long firstSkuId = spu.skus().get(0).id();
        Long secondSkuId = spu.skus().get(1).id();
        assertSalesInQueries(spu.id(), 0);
        assertTrue(fixture.productInventoryService.reserveStock("PAID", firstSkuId, 2));
        assertTrue(fixture.productInventoryService.reserveStock("PAID", secondSkuId, 3));
        // 预占不计销量，支付确认才累计。
        assertSalesInQueries(spu.id(), 0);
        var lines =
                List.of(
                        new OrderStockLineDTO(firstSkuId, 2),
                        new OrderStockLineDTO(secondSkuId, 3));
        var event = paidEvent("PAID", lines);

        handler.paid(event);
        handler.paid(event);
        // 同业务键、不同事件号也不能重复记账。
        handler.paid(paidEvent("PAID", lines));

        assertSalesInQueries(spu.id(), 5);
        fixture.productSpuGateway.shelf(spu.id(), false);
        fixture.productSpuGateway.shelf(spu.id(), true);
        assertSalesInQueries(spu.id(), 5);
    }

    @Test
    void paid_whenLaterLineFails_shouldRollbackSalesReceiptsAndEarlierConfirmation()
            throws Exception {
        var spu = createOnShelf("ROLLBACK");
        Long firstSkuId = spu.skus().get(0).id();
        Long secondSkuId = spu.skus().get(1).id();
        assertTrue(fixture.productInventoryService.reserveStock("RETRY", firstSkuId, 2));
        assertTrue(fixture.productInventoryService.reserveStock("RETRY", secondSkuId, 1));
        var invalid =
                paidEvent(
                        "RETRY",
                        List.of(
                                new OrderStockLineDTO(firstSkuId, 2),
                                new OrderStockLineDTO(secondSkuId, 2)));

        assertThrows(IllegalStateException.class, () -> handler.paid(invalid));

        assertSalesInQueries(spu.id(), 0);
        assertEquals(
                "RESERVED",
                fixture.productInventoryService.reservationStatus("RETRY", firstSkuId).status());
        assertEquals(
                "RESERVED",
                fixture.productInventoryService.reservationStatus("RETRY", secondSkuId).status());
        assertNoPaidReceipt();
        handler.paid(
                paidEvent(
                        "RETRY",
                        List.of(
                                new OrderStockLineDTO(firstSkuId, 2),
                                new OrderStockLineDTO(secondSkuId, 1))));
        assertSalesInQueries(spu.id(), 3);
    }

    @Test
    void paid_whenSalesWriteFails_shouldRetryWithoutLosingOrDoubleCountingSales() throws Exception {
        var spu = createOnShelf("WRITE-FAIL");
        Long skuId = spu.skus().get(0).id();
        assertTrue(fixture.productInventoryService.reserveStock("WRITE-FAIL", skuId, 3));
        var event = paidEvent("WRITE-FAIL", List.of(new OrderStockLineDTO(skuId, 3)));
        try (var statement = fixture.schemaConnection.createStatement()) {
            statement.execute(
                    "ALTER TABLE product_spu ADD CONSTRAINT reject_sales CHECK(sales_count < 3)");
        }

        assertThrows(RuntimeException.class, () -> handler.paid(event));

        assertEquals(0, fixture.productSpuGateway.salesCount(spu.id()));
        assertEquals(
                "RESERVED",
                fixture.productInventoryService.reservationStatus("WRITE-FAIL", skuId).status());
        assertNoPaidReceipt();
        try (var statement = fixture.schemaConnection.createStatement()) {
            statement.execute("ALTER TABLE product_spu DROP CONSTRAINT reject_sales");
        }
        handler.paid(event);
        handler.paid(event);
        assertSalesInQueries(spu.id(), 3);
    }

    @Test
    void paid_whenDifferentOrdersArriveConcurrently_shouldNotLoseSalesUpdates() throws Exception {
        var spu = createOnShelf("CONCURRENT");
        Long firstSkuId = spu.skus().get(0).id();
        Long secondSkuId = spu.skus().get(1).id();
        assertTrue(fixture.productInventoryService.reserveStock("ORDER-A", firstSkuId, 2));
        assertTrue(fixture.productInventoryService.reserveStock("ORDER-B", secondSkuId, 3));
        var first = paidEvent("ORDER-A", List.of(new OrderStockLineDTO(firstSkuId, 2)));
        var second = paidEvent("ORDER-B", List.of(new OrderStockLineDTO(secondSkuId, 3)));
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var firstResult =
                    pool.submit(
                            () -> {
                                assertTrue(start.await(10, TimeUnit.SECONDS));
                                handler.paid(first);
                                return true;
                            });
            var secondResult =
                    pool.submit(
                            () -> {
                                assertTrue(start.await(10, TimeUnit.SECONDS));
                                handler.paid(second);
                                return true;
                            });
            start.countDown();
            assertTrue(firstResult.get(10, TimeUnit.SECONDS));
            assertTrue(secondResult.get(10, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }
        assertSalesInQueries(spu.id(), 5);
    }

    private ProductSpu createOnShelf(String code) {
        var spu =
                fixture.create(
                        code, fixture.draft(code + "-A", 10), fixture.draft(code + "-B", 10));
        fixture.productSpuGateway.shelf(spu.id(), true);
        return spu;
    }

    private void assertSalesInQueries(Long spuId, int expected) {
        assertEquals(expected, fixture.productSpuGateway.salesCount(spuId));
        var page =
                fixture.components
                        .get(StoreProductPageQueryExe.class)
                        .execute(new StorePageQry(null, null, 1, 20));
        assertEquals(1, page.total());
        assertEquals(expected, page.records().get(0).salesCount());
        var detail = fixture.components.get(StoreProductDetailQueryExe.class).execute(spuId);
        assertNotNull(detail);
        assertEquals(expected, detail.salesCount());
    }

    private void assertNoPaidReceipt() throws Exception {
        try (var statement = fixture.schemaConnection.createStatement();
                var rows =
                        statement.executeQuery(
                                "SELECT COUNT(*) FROM product_message_inbox WHERE consumer_key='product.order_paid.v1'")) {
            assertTrue(rows.next());
            assertEquals(0, rows.getInt(1));
        }
    }

    private CommerceEventEnvelope paidEvent(String orderNo, List<OrderStockLineDTO> lines) {
        var payload = new OrderPaidEvent(orderNo, 1L, BigDecimal.TEN, 0, 0, Map.of(), lines);
        return new CommerceEventEnvelope(
                UUID.randomUUID().toString(),
                CommerceEventTopics.ORDER_PAID,
                1,
                orderNo,
                orderNo,
                Instant.now(),
                null,
                JsonUtils.toJsonStr(payload));
    }
}
