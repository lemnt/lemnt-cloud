package com.lemnt.trade.start.checkout;

import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.mall.application.product.service.ProductInventoryService;
import com.lemnt.mall.domain.product.gateway.ProductGateway;
import com.lemnt.mall.domain.product.model.Product;
import com.lemnt.mall.application.checkout.cmd.CartSaveCmd;
import com.lemnt.mall.application.checkout.cmd.CheckoutSubmitCmd;
import com.lemnt.mall.application.checkout.executor.CartSaveCmdExe;
import com.lemnt.mall.application.checkout.executor.CheckoutReceiptQueryExe;
import com.lemnt.trade.application.payment.executor.MemberPaymentStatusQueryExe;
import com.lemnt.mall.application.checkout.qry.CheckoutQuoteQry;
import com.lemnt.trade.application.payment.qry.MemberPaymentStatusQry;
import com.lemnt.trade.application.order.service.OrderLifecycleService;
import com.lemnt.mall.application.checkout.service.CheckoutMemberService;
import com.lemnt.mall.application.checkout.service.CheckoutService;
import com.lemnt.mall.application.checkout.service.OrderPlacementService;
import com.lemnt.mall.application.checkout.service.SettlementService;
import com.lemnt.trade.application.fulfillment.cmd.RefundRejectCmd;
import com.lemnt.trade.application.fulfillment.cmd.RefundRequestCmd;
import com.lemnt.trade.application.fulfillment.cmd.ShipmentCreateCmd;
import com.lemnt.trade.application.fulfillment.executor.OrderPageQueryExe;
import com.lemnt.trade.application.fulfillment.executor.RefundPageQueryExe;
import com.lemnt.trade.application.fulfillment.qry.OrderPageQry;
import com.lemnt.trade.application.fulfillment.qry.RefundPageQry;
import com.lemnt.trade.application.fulfillment.service.OrderFulfillmentService;
import com.lemnt.mall.application.checkout.cmd.CheckoutDirectCmd;
import com.lemnt.trade.application.order.executor.TradeOrderCloseCmdExe;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.application.outbox.executor.TradeOutboxDispatchCmdExe;
import com.lemnt.trade.application.payment.executor.PaymentNotifyCmdExe;
import com.lemnt.trade.application.payment.executor.TradePaymentCreateCmdExe;
import com.lemnt.mall.application.store.cmd.CartAddCmd;
import com.lemnt.mall.application.store.cmd.CouponClaimCmd;
import com.lemnt.mall.application.store.cmd.CouponPublicClaimCmd;
import com.lemnt.mall.application.store.qry.PromotionPageQry;
import com.lemnt.mall.application.store.service.StorefrontService;
import com.lemnt.mall.domain.catalog.gateway.ProductCatalogGateway;
import com.lemnt.mall.domain.catalog.model.ProductSnapshot;
import com.lemnt.mall.domain.checkout.gateway.CartGateway;
import com.lemnt.mall.domain.checkout.gateway.CheckoutAccountGateway;
import com.lemnt.mall.domain.checkout.gateway.CheckoutCartGateway;
import com.lemnt.mall.domain.checkout.gateway.CouponGateway;
import com.lemnt.mall.domain.checkout.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.checkout.gateway.SettlementGateway;
import com.lemnt.mall.domain.checkout.model.CheckoutRules;
import com.lemnt.mall.domain.checkout.model.CouponTemplate;
import com.lemnt.mall.domain.checkout.model.Settlement;
import com.lemnt.trade.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.trade.domain.fulfillment.gateway.RefundRequestGateway;
import com.lemnt.trade.domain.fulfillment.gateway.ShipmentGateway;
import com.lemnt.mall.domain.member.model.MemberSnapshot;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.mall.domain.order.model.OrderSnapshot;
import com.lemnt.trade.domain.outbox.gateway.TradeOutboxGateway;
import com.lemnt.trade.domain.outbox.model.OutboxEvent;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.payment.model.VerifiedPaymentNotification;
import com.lemnt.mall.domain.store.gateway.CartAddRequestGateway;
import com.lemnt.mall.domain.store.gateway.CouponPublicClaimGateway;
import com.lemnt.trade.infrastructure.outbox.gateway.TradeOutboxGatewayImpl;
import com.lemnt.trade.infrastructure.outbox.mapper.TradeOutboxMapper;
import com.lemnt.trade.infrastructure.payment.gateway.TradePaymentGatewayImpl;
import com.lemnt.trade.infrastructure.payment.mapper.TradePaymentMapper;
import com.lemnt.trade.start.TableTestComponents;
import com.lemnt.trade.start.TradeOutboxScheduler;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Two isolated databases, actual production gateways/mappers and transaction proxies. RPC is a test
 * bridge; this is not a Nacos/Dubbo runtime or real payment-channel test.
 */
class CheckoutFlowIntegrationTest {
    TableTestComponents tradeComponents, mallComponents;
    ProductGateway productGateway;
    ProductInventoryService productInventoryService;
    CheckoutAccountGateway checkoutAccountGateway;
    CheckoutCartGateway checkoutCartGateway;
    CartGateway cartGateway;
    CouponTemplateGateway couponTemplateGateway;
    CouponGateway couponGateway;
    CheckoutService checkoutService;
    InventoryConfirmationGateway inventoryConfirmationGateway;
    ShipmentGateway shipmentGateway;
    RefundRequestGateway refundRequestGateway;
    TradeOrderGateway tradeOrderGateway;
    OrderPersistenceService orderPersistenceService;

    JdbcDataSource tradeSource, mallSource;
    Connection tradeSchema, mallSchema;
    StorefrontService store;
    SettlementGateway settlements;
    com.lemnt.mall.domain.checkout.gateway.SettlementOrderGateway settlementOrderGateway;
    com.lemnt.mall.domain.checkout.gateway.SettlementAbandonGateway settlementAbandonGateway;
    Map<String, Settlement> settlementCache =
    new ConcurrentHashMap<>();
    OrderFulfillmentService fulfillmentService;
    TradeOutboxGateway outbox;
    TradePaymentGateway payments;
    OrderPlacementService placement;
    OrderLifecycleService lifecycle;
    TradeOrderCloseCmdExe closer;
    TradePaymentCreateCmdExe paymentCreate;
    PaymentNotifyCmdExe notify;
    interface CatalogBridge extends ProductCatalogGateway, com.lemnt.trade.domain.inventory.gateway.InventoryGateway {}
    CatalogBridge bridge;
    com.lemnt.mall.domain.order.gateway.OrderGateway orderRpc;
    com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway benefits;
    Long sku;
    boolean loseNextOrderResponse;
    boolean loseNextReserveResponse;
    boolean loseNextConfirmResponse;
    Map<String, OrderSnapshot> submittedOrders = new java.util.concurrent.ConcurrentHashMap<>();
    Map<String, String> quoteTokens = new HashMap<>();

    JdbcDataSource source() {
        var source = new JdbcDataSource();
        source.setURL(
                "jdbc:h2:mem:checkout_"
                + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
        return source;
    }

    SqlSessionTemplate template(JdbcDataSource source, Class<?>... mappers) throws Exception {
        var config = new MybatisConfiguration();
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        config.addInterceptor(interceptor);
        config.setMapUnderscoreToCamelCase(true);
        var bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(source);
        bean.setConfiguration(config);
        bean.setGlobalConfig(new GlobalConfig().setMetaObjectHandler(new MyMetaObjectHandler()));
        var factory = bean.getObject();
        config.addMappers(source == tradeSource
                ? "com.lemnt.trade.infrastructure" : "com.lemnt.mall.infrastructure");
        return new SqlSessionTemplate(factory);
    }

    @SuppressWarnings("unchecked")
    <T> T tx(T target, JdbcDataSource source) {
        var proxy = new ProxyFactory(target);
        proxy.setProxyTargetClass(true);
        proxy.addAdvice(
                new TransactionInterceptor(
                new DataSourceTransactionManager(source),
                new AnnotationTransactionAttributeSource()));
        return (T) proxy.getProxy();
    }

    @BeforeEach
    void setup() throws Exception {
        tradeSource = source();
        tradeSource.setURL(tradeSource.getURL() + ";INIT=SET TIME ZONE 'UTC'");
        mallSource = source();
        tradeSchema = tradeSource.getConnection();
        mallSchema = mallSource.getConnection();
        ScriptUtils.executeSqlScript(
                tradeSchema, new ClassPathResource("trade-sql/V1__trade_order.sql"));
        ScriptUtils.executeSqlScript(
                tradeSchema, new ClassPathResource("trade-sql/V2__trade_payment_outbox.sql"));
        try (var s = tradeSchema.createStatement()) {
            // Existing V3 fixture: H2 lacks its MySQL multi-action ALTER syntax. New V4 is
            // unmodified below.
            s.execute("ALTER TABLE trade_outbox ADD lease_token VARCHAR(36)");
            s.execute("ALTER TABLE trade_outbox ADD lease_until TIMESTAMP");
            s.execute(
                    "ALTER TABLE trade_outbox ADD next_attempt_at TIMESTAMP DEFAULT"
                    + " CURRENT_TIMESTAMP NOT NULL");
        }
        ScriptUtils.executeSqlScript(
                mallSchema, new ClassPathResource("mall-sql/V10__cart_coupon_checkout.sql"));
        ScriptUtils.executeSqlScript(
                mallSchema, new ClassPathResource("mall-sql/V1__mall_product.sql"));
        ScriptUtils.executeSqlScript(
                mallSchema, new ClassPathResource("mall-sql/V2__mall_catalog.sql"));
        ScriptUtils.executeSqlScript(
                tradeSchema, new ClassPathResource("trade-sql/V5__order_fulfillment.sql"));
        ScriptUtils.executeSqlScript(
                mallSchema, new ClassPathResource("mall-sql/V12__settlement_order.sql"));
        ScriptUtils.executeSqlScript(
                mallSchema, new ClassPathResource("mall-sql/V13__settlement_abandon.sql"));
        ScriptUtils.executeSqlScript(
                mallSchema, new ClassPathResource("mall-sql/V14__storefront_marketing.sql"));
        var trade =
        template(tradeSource);
        var products = template(mallSource);
        tradeComponents = new TableTestComponents(trade, tradeSource);
        mallComponents = new TableTestComponents(products, mallSource);
        var provider = new com.lemnt.trade.application.dubbo.TradeOrderClientImpl(
                tradeComponents.get(com.lemnt.trade.application.order.service.OrderSubmissionService.class),
                tradeComponents.get(OrderPersistenceService.class));
        var client = new com.lemnt.trade.client.TradeOrderClient() {
            public com.lemnt.trade.client.dto.OrderDTO create(com.lemnt.trade.client.bo.OrderCreateBo value) {
                var saved = provider.create(value);
                if (loseNextOrderResponse) {
                    loseNextOrderResponse = false;
                    throw new IllegalStateException("交易订单已提交，但 RPC 响应丢失");
                }
                return saved;
            }
            public com.lemnt.trade.client.dto.OrderDTO getByOrderNo(String no) {
                return provider.getByOrderNo(no);
            }
        };
        var orderAdapter = new com.lemnt.mall.infrastructure.order.gateway.OrderGatewayImpl();
        org.springframework.test.util.ReflectionTestUtils.setField(orderAdapter, "client", client);
        orderRpc = orderAdapter;
        mallComponents.register(com.lemnt.mall.domain.order.gateway.OrderGateway.class, orderRpc);

        productGateway = mallComponents.get(ProductGateway.class);
        productInventoryService = mallComponents.get(ProductInventoryService.class);
        checkoutAccountGateway = mallComponents.get(CheckoutAccountGateway.class);
        checkoutCartGateway = mallComponents.get(CheckoutCartGateway.class);
        cartGateway = mallComponents.get(CartGateway.class);
        couponTemplateGateway = mallComponents.get(CouponTemplateGateway.class);
        couponGateway = mallComponents.get(CouponGateway.class);
        checkoutService = mallComponents.get(CheckoutService.class);
        inventoryConfirmationGateway = tradeComponents.get(InventoryConfirmationGateway.class);
        shipmentGateway = tradeComponents.get(ShipmentGateway.class);
        refundRequestGateway = tradeComponents.get(RefundRequestGateway.class);
        tradeOrderGateway = tradeComponents.get(TradeOrderGateway.class);
        orderPersistenceService = tradeComponents.get(OrderPersistenceService.class);

        settlementOrderGateway = mallComponents.get(com.lemnt.mall.domain.checkout.gateway.SettlementOrderGateway.class);
        settlementAbandonGateway = mallComponents.get(com.lemnt.mall.domain.checkout.gateway.SettlementAbandonGateway.class);
        settlements = new SettlementGateway() {
            public void cache(Settlement value) {
                settlementCache.put(value.id(), value);
            }

            public void cacheOrder(OrderSnapshot value) {
                submittedOrders.put(value.orderNo(), value);
            }

            public OrderSnapshot cachedOrder(String orderNo) {
                return submittedOrders.get(orderNo);
            }

            public Settlement cached(String id) {
                return settlementCache.get(id);
            }

        };

        outbox =
        tx(
                new TradeOutboxGatewayImpl(trade.getMapper(TradeOutboxMapper.class)),
                tradeSource);
        payments =
        tx(
                new TradePaymentGatewayImpl(trade.getMapper(TradePaymentMapper.class)),
                tradeSource);

        sku =
        productGateway.save(
                Product.create(
                "SKU1", "sku", null, null, new BigDecimal("10.00"), 10))
            .id();
        productGateway.updateStatus(sku, "ON_SHELF");
        bridge =
        new CatalogBridge() {
            public ProductSnapshot findOnShelf(Long id) {
                var p = productGateway.findSellableById(id);
                return p == null
                ? null
                : new ProductSnapshot(
                        p.id(), p.productCode(), p.name(), p.price(), p.stock());
            }

            public Map<Long, ProductSnapshot> findOnShelfBatch(List<Long> ids) {
                Map<Long, ProductSnapshot> result = new HashMap<>();
                for (var p : productGateway.findSellableByIds(ids)) {
                    result.put(
                            p.id(),
                            new ProductSnapshot(
                            p.id(),
                            p.productCode(),
                            p.name(),
                            p.price(),
                            p.stock()));
                }
                return result;
            }

            public boolean reserveStock(String no, Long id, Integer quantity) {
                boolean result = productInventoryService.reserveStock(no, id, quantity);
                if (loseNextReserveResponse) {
                    loseNextReserveResponse = false;
                    throw new IllegalStateException(
                            "simulated lost reserve response after mall commit");
                }
                return result;
            }

            public void cancelReservation(String no, Long id, Integer quantity) {
                productInventoryService.cancelReservation(no, id, quantity);
            }

            public void releaseStock(String no, Long id) {
                productInventoryService.releaseStock(no, id);
            }

            public void confirmStock(String no, Long id) {
                productInventoryService.confirmStock(no, id);
                if (loseNextConfirmResponse) {
                    loseNextConfirmResponse = false;
                    throw new IllegalStateException("确认库存已提交但RPC响应丢失");
                }
            }
        };
        var member = new CheckoutMemberService(id -> new MemberSnapshot(id, 1));
        store =
        tx(
                new StorefrontService(mallComponents.get(CartAddRequestGateway.class), mallComponents.get(CartGateway.class), mallComponents.get(CheckoutAccountGateway.class), mallComponents.get(CheckoutService.class), mallComponents.get(CouponGateway.class), mallComponents.get(CouponPublicClaimGateway.class), mallComponents.get(CouponTemplateGateway.class), member, bridge),
                mallSource);
        placement =
        new OrderPlacementService(tx(new com.lemnt.mall.application.checkout.service.CheckoutPreparationService(mallComponents.get(CartGateway.class), settlementOrderGateway, settlementAbandonGateway, mallComponents.get(CheckoutAccountGateway.class), mallComponents.get(CheckoutCartGateway.class), mallComponents.get(CheckoutService.class), mallComponents.get(CouponGateway.class), mallComponents.get(CouponTemplateGateway.class), orderRpc, settlements, bridge, member), mallSource), orderRpc);

        fulfillmentService =
        tx(new OrderFulfillmentService(tradeComponents.get(InventoryConfirmationGateway.class), tradeComponents.get(OrderPersistenceService.class), tradeComponents.get(RefundRequestGateway.class), tradeComponents.get(ShipmentGateway.class), tradeComponents.get(TradeOrderGateway.class), payments), tradeSource);
        lifecycle =
        tx(
                new OrderLifecycleService(tradeComponents.get(InventoryConfirmationGateway.class), tradeComponents.get(OrderPersistenceService.class), tradeComponents.get(TradeOrderGateway.class), outbox),
                tradeSource);
        closer =
        tx(
                new TradeOrderCloseCmdExe(tradeComponents.get(OrderPersistenceService.class), tradeComponents.get(TradeOrderGateway.class), outbox, payments, p -> false),
                tradeSource);
        paymentCreate = tx(new TradePaymentCreateCmdExe(tradeComponents.get(OrderPersistenceService.class), payments), tradeSource);
        notify = tx(new PaymentNotifyCmdExe(tradeComponents.get(OrderPersistenceService.class), tradeComponents.get(TradeOrderGateway.class), payments, outbox), tradeSource);
        var handler = tx(new com.lemnt.mall.application.dubbo.CheckoutLifecycleClientImpl(
                checkoutService, orderRpc), mallSource);
        benefits = new com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway() {
            public void reserved(String no) { handler.reserved(no); }
            public void paid(String no) { handler.paid(no); }
            public void closed(String no) { handler.closed(no); }
        };
        checkoutService.saveCart(1L, sku, 2, true);
    }

    @AfterEach
    void close() throws Exception {
        if (tradeSchema != null) {
            tradeSchema.close();
        }
        if (mallSchema != null) {
            mallSchema.close();
        }
    }

    TradeOutboxDispatchCmdExe worker() {
        return new TradeOutboxDispatchCmdExe(tradeComponents.get(OrderPersistenceService.class), outbox, bridge, benefits, lifecycle);
    }

    CouponTemplate campaign(int quota, BigDecimal amount) {
        var now = checkoutAccountGateway.now();
        return couponTemplateGateway.createTemplate(
                CheckoutRules.template(
                new CouponTemplate(
                null,
                "C" + UUID.randomUUID(),
                "coupon",
                "FIXED",
                BigDecimal.ZERO,
                amount,
                null,
                null,
                quota,
                0,
                now.minusDays(1),
                now.plusDays(1),
                true)));
    }

    OrderSnapshot place(String key, Long coupon) {
        String token =
        quoteTokens.computeIfAbsent(
                key,
                k ->
                CheckoutRules.quoteToken(
                placement.quote(
                new CheckoutQuoteQry(1L, List.of(sku), coupon))));
        return placement.checkoutCart(new CheckoutSubmitCmd(1L, key, List.of(sku), coupon, token));
    }

    void sql(JdbcDataSource source, String sql) throws Exception {
        try (var c = source.getConnection();
                var s = c.createStatement()) {
            s.execute(sql);
        }
    }

    long count(String table) throws Exception {
        try (var c = (table.startsWith("mall_") ? mallSource : tradeSource).getConnection();
                var s = c.createStatement();
                var r = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            r.next();
            return r.getLong(1);
        }
    }

    @Test
    void successfulCheckoutUsesServerPricesLocksCouponAndConsumesCartAfterReservation() {
        var template = campaign(10, new BigDecimal("3.00"));
        var coupon = checkoutService.issue(1L, template.id());
        var quote = placement.quote(new CheckoutQuoteQry(1L, List.of(sku), coupon.id()));
        assertEquals(new BigDecimal("17.00"), quote.payableAmount());
        var order = place("REQ1", coupon.id());
        assertEquals("RESERVING", order.status());
        assertTrue(
                Duration.between(order.createTime(), tradeOrderGateway.databaseNow())
                .abs()
                .getSeconds()
                < 2);
        assertEquals(10, productGateway.findById(sku).stock());
        assertThrows(IllegalStateException.class, () -> place("SECOND-KEY", null));
        assertEquals("LOCKED", couponGateway.coupon(1L, coupon.id(), false).status());
        assertEquals(1, cartGateway.cart(1L).size());
        assertThrows(
                IllegalStateException.class,
                () -> paymentCreate.execute(order.orderNo(), "ALIPAY"));
        assertEquals(1, worker().execute(50));
        assertEquals("PENDING_PAYMENT", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals(8, productGateway.findById(sku).stock());
        assertTrue(cartGateway.cart(1L).isEmpty());
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        var notice =
        new VerifiedPaymentNotification(
                payment.paymentNo(),
                order.orderNo(),
                "ALIPAY",
                "CHANNEL1",
                new BigDecimal("17.00"),
                "N1");
        notify.execute(notice);
        notify.execute(notice);
        assertEquals("LOCKED", couponGateway.coupon(1L, coupon.id(), false).status());
        assertEquals(1, worker().execute(50));
        assertEquals("USED", couponGateway.coupon(1L, coupon.id(), false).status());
        assertEquals(8, productGateway.findById(sku).stock());
    }

    @Test
    void idempotentRetryReturnsOriginalPriceEvenAfterCartDeletionAndCatalogPriceChange()
    throws Exception {
        var order = place("REQ1", null);
        worker().execute(50);
        sql(mallSource, "UPDATE mall_product SET price=99.00 WHERE id=" + sku);
        assertEquals(order.orderNo(), place("REQ1", null).orderNo());
        assertEquals(new BigDecimal("20.00"), checkoutService.receipt(order.orderNo()).payableAmount());
        assertEquals(1, count("trade_order"));
        assertThrows(IllegalStateException.class, () -> place("REQ1", 99L));
    }

    @Test
    void lostRpcResponseIsRetriedAfterRestartWithoutDoubleDebit() throws Exception {
        var order = place("LOST", null);
        loseNextReserveResponse = true;
        assertEquals(0, worker().execute(50));
        assertEquals(8, productGateway.findById(sku).stock());
        assertEquals("RESERVING", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        sql(tradeSource, "UPDATE trade_outbox SET next_attempt_at=TIMESTAMP '2000-01-01 00:00:00'");
        assertEquals(1, worker().execute(50));
        assertEquals(8, productGateway.findById(sku).stock());
        assertEquals("PENDING_PAYMENT", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void closingAfterAmbiguousReserveReleasesCouponAndFencesLateRequests() throws Exception {
        var coupon = checkoutService.issue(1L, campaign(10, BigDecimal.ONE).id());
        var order = place("LOST-CLOSE", coupon.id());
        loseNextReserveResponse = true;
        worker().execute(50);
        assertTrue(closer.execute(order.orderNo()));
        worker().execute(50);
        assertEquals(10, productGateway.findById(sku).stock());
        assertEquals("AVAILABLE", couponGateway.coupon(1L, coupon.id(), false).status());
        assertFalse(productInventoryService.reserveStock(order.orderNo(), sku, 2));
        assertEquals(1, cartGateway.cart(1L).size());
    }

    @Test
    void stockRejectionDurablyClosesAndCancelsUnattemptedProducts() {
        var order = place("REJECT", null);
        productGateway.updateStatus(sku, "OFF_SHELF");
        assertEquals(1, worker().execute(50));
        assertEquals("CLOSED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        worker().execute(50);
        productGateway.updateStatus(sku, "ON_SHELF");
        assertFalse(productInventoryService.reserveStock(order.orderNo(), sku, 2));
        assertEquals(10, productGateway.findById(sku).stock());
    }

    @Test
    void concurrentCouponIssuanceNeverExceedsQuotaAndDuplicatesAreIdempotent() throws Exception {
        var template = campaign(2, BigDecimal.ONE);
        var pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (long i = 1; i <= 8; i++) {
                final long member = i;
                tasks.add(
                        () -> {
                    try {
                        checkoutService.issue(member, template.id());
                        return true;
                    } catch (IllegalStateException exhausted) {
                        return false;
                    }
                });
            }
            long successes = 0;
            for (var result : pool.invokeAll(tasks)) {
                if (result.get()) {
                    successes++;
                }
            }
            assertEquals(2, successes);
            assertEquals(2, couponTemplateGateway.template(template.id()).issuedCount());
            for (long i = 1; i <= 8; i++) {
                if (!couponGateway.coupons(i).isEmpty()) {
                    assertEquals(
                            couponGateway.coupons(i).get(0).id(), checkoutService.issue(i, template.id()).id());
                }
            }
            assertEquals(2, couponTemplateGateway.template(template.id()).issuedCount());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void ownershipSelectedStateAndDisabledTemplatesRejectCheckoutWithoutSideEffects()
    throws Exception {
        var template = campaign(10, BigDecimal.ONE);
        var foreign = checkoutService.issue(2L, template.id());
        assertThrows(IllegalArgumentException.class, () -> place("FOREIGN", foreign.id()));
        var own = checkoutService.issue(1L, template.id());
        couponTemplateGateway.enableTemplate(template.id(), false);
        assertThrows(IllegalArgumentException.class, () -> place("DISABLED", own.id()));
        checkoutService.saveCart(1L, sku, 2, false);
        assertThrows(IllegalArgumentException.class, () -> place("NOT-SELECTED", null));
        assertEquals(0, count("trade_order"));
        assertEquals(0, count("trade_outbox"));
    }

    @Test
    void cartMutationOrDeleteReaddDuringReservationIsNotRemovedByFinalizer() {
        place("V1", null);
        checkoutService.saveCart(1L, sku, 3, true);
        worker().execute(50);
        assertEquals(3, cartGateway.cart(1L).get(0).quantity());
        var order = place("V2", null);
        var oldId = cartGateway.cart(1L).get(0).id();
        checkoutService.deleteCart(1L, sku);
        checkoutService.saveCart(1L, sku, 1, true);
        worker().execute(50);
        assertNotEquals(oldId, cartGateway.cart(1L).get(0).id());
        assertEquals(1, cartGateway.cart(1L).get(0).quantity());
        assertEquals("PENDING_PAYMENT", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void zeroPayableQuoteIsExplicitlyUnsupportedAtPlacementAndRollsBackLocks() throws Exception {
        var coupon = checkoutService.issue(1L, campaign(10, new BigDecimal("50.00")).id());
        assertEquals(
                new BigDecimal("0.00"),
                placement
                .quote(new CheckoutQuoteQry(1L, List.of(sku), coupon.id()))
                .payableAmount());
        assertThrows(IllegalArgumentException.class, () -> place("ZERO", coupon.id()));
        assertEquals("AVAILABLE", couponGateway.coupon(1L, coupon.id(), false).status());
        assertEquals(0, count("trade_order"));
    }

    @Test
    void couponInvariantFailureRetainsPaidOrderAndRetriesMallCallback() throws Exception {
        var coupon = checkoutService.issue(1L, campaign(10, BigDecimal.ONE).id());
        var order = place("COUPON-INVARIANT", coupon.id());
        worker().execute(50);
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        sql(mallSource,
                "UPDATE mall_coupon SET status='AVAILABLE',order_no=NULL WHERE id=" + coupon.id());
        var notice =
        new VerifiedPaymentNotification(
                payment.paymentNo(),
                order.orderNo(),
                "ALIPAY",
                "CH1",
                order.totalAmount(),
                "N1");
        notify.execute(notice);
        assertEquals(0, worker().execute(50));
        assertEquals("SUCCESS", payments.findByPaymentNo(payment.paymentNo()).status());
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void reservationTimeoutIsNotStarvedByBlockedPendingPayments() throws Exception {
        var order = place("EXPIRE", null);
        sql(
                tradeSource,
                "UPDATE trade_order SET create_time=TIMESTAMP '2001-01-01 00:00:00' WHERE"
                + " order_no='"
                + order.orderNo()
                + "'");
        try (var connection = tradeSource.getConnection();
                var statement = connection.createStatement()) {
            for (int i = 0; i < 50; i++) {
                statement.execute(
                        "INSERT INTO"
                        + " trade_order(order_no,member_id,total_amount,status,create_time)"
                        + " VALUES('BLOCK"
                        + i
                        + "',1,1,'PENDING_PAYMENT',TIMESTAMP '2000-01-01 00:00:00')");
                statement.execute(
                        "INSERT INTO trade_payment(payment_no,order_no,channel,amount,status)"
                        + " VALUES('PAYBLOCK"
                        + i
                        + "','BLOCK"
                        + i
                        + "','ALIPAY',1,'WAITING')");
            }
        }
        new TradeOutboxScheduler(worker(), closer, tradeOrderGateway).closeExpired();
        assertEquals("CLOSED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals("PENDING_PAYMENT", orderPersistenceService.findByOrderNo("BLOCK0").status());
    }

    @Test
    void changedPriceAfterPreviewRequiresExplicitReconfirmation() throws Exception {
        String token =
        CheckoutRules.quoteToken(
                placement.quote(new CheckoutQuoteQry(1L, List.of(sku), null)));
        sql(mallSource, "UPDATE mall_product SET price=11.00 WHERE id=" + sku);
        assertThrows(
                IllegalStateException.class,
                () ->
                placement.checkoutCart(
                new CheckoutSubmitCmd(1L, "STALE", List.of(sku), null, token)));
        assertEquals(0, count("trade_order"));
        assertEquals(10, productGateway.findById(sku).stock());
    }

    @Test
    void unavailableCartItemCanBeDeselectedButNotNewlySelected() {
        productGateway.updateStatus(sku, "OFF_SHELF");
        var saver =
        tx(
                new CartSaveCmdExe(mallComponents.get(CartGateway.class), mallComponents.get(CheckoutAccountGateway.class), mallComponents.get(CheckoutService.class), new CheckoutMemberService(id -> new MemberSnapshot(id, 1)), bridge),
                mallSource);
        assertFalse(saver.execute(new CartSaveCmd(1L, sku, 2, false)).selected());
        assertThrows(
                IllegalArgumentException.class,
                () -> saver.execute(new CartSaveCmd(1L, sku, 2, true)));
    }

    @Test
    void failedTradeOutboxInsertRetainsMallIntentAndRetryCreatesOneOrder() throws Exception {
        var coupon = checkoutService.issue(1L, campaign(10, BigDecimal.ONE).id());
        sql(tradeSource, "ALTER TABLE trade_outbox ADD CONSTRAINT test_reject_outbox CHECK(1=0)");
        assertThrows(RuntimeException.class, () -> place("DB-FAIL", coupon.id()));
        assertEquals(0, count("trade_order"));
        assertEquals(1, count("mall_checkout"));
        assertEquals("LOCKED", couponGateway.coupon(1L, coupon.id(), false).status());
        assertEquals(10, productGateway.findById(sku).stock());
        sql(tradeSource, "ALTER TABLE trade_outbox DROP CONSTRAINT test_reject_outbox");
        var order = place("DB-FAIL", coupon.id());
        assertEquals(order.orderNo(), place("DB-FAIL", coupon.id()).orderNo());
        assertEquals(1, count("trade_order"));
        assertEquals(1, count("trade_outbox"));
    }

    @Test
    void expiredCouponCannotBecomeLockedOrCreateOrder() throws Exception {
        var coupon = checkoutService.issue(1L, campaign(10, BigDecimal.ONE).id());
        sql(mallSource,
                "UPDATE mall_coupon_template SET valid_from=TIMESTAMP '2000-01-01"
                + " 00:00:00',valid_until=TIMESTAMP '2001-01-01 00:00:00' WHERE id="
                + coupon.templateId());
        assertThrows(IllegalArgumentException.class, () -> place("EXPIRED", coupon.id()));
        assertEquals(0, count("trade_order"));
        assertEquals("AVAILABLE", couponGateway.coupon(1L, coupon.id(), false).status());
    }

    @Test
    void concurrentIdenticalCheckoutKeyCreatesOnlyOneDurableIntent() throws Exception {
        var token =
        CheckoutRules.quoteToken(
                placement.quote(new CheckoutQuoteQry(1L, List.of(sku), null)));
        var command = new CheckoutSubmitCmd(1L, "SAME", List.of(sku), null, token);
        var pool = Executors.newFixedThreadPool(4);
        try {
            List<Callable<String>> tasks = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                tasks.add(() -> placement.checkoutCart(command).orderNo());
            }
            Set<String> orderNos = new HashSet<>();
            for (var result : pool.invokeAll(tasks)) {
                orderNos.add(result.get());
            }
            assertEquals(1, orderNos.size());
            assertEquals(1, count("trade_order"));
            assertEquals(1, count("trade_outbox"));
            assertEquals(10, productGateway.findById(sku).stock());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void paymentAmountMustMatchOrderPayableNotOnlyThePaymentRow() throws Exception {
        var order = place("AMOUNT", null);
        worker().execute(50);
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        sql(
                tradeSource,
                "UPDATE trade_payment SET amount=1.00 WHERE payment_no='"
                + payment.paymentNo()
                + "'");
        var notice =
        new VerifiedPaymentNotification(
                payment.paymentNo(),
                order.orderNo(),
                "ALIPAY",
                "CH1",
                BigDecimal.ONE,
                "N1");
        assertThrows(IllegalStateException.class, () -> notify.execute(notice));
        assertEquals("PENDING_PAYMENT", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void twoConcurrentRequestsCannotSpendTheSameCouponTwice() throws Exception {
        var coupon = checkoutService.issue(1L, campaign(10, BigDecimal.ONE).id());
        var pool = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Boolean>> tasks =
            List.of(
                    () -> {
                try {
                    placement.createDirect(
                            new CheckoutDirectCmd(
                            1L,
                            List.of(
                            new com.lemnt.mall.application.checkout.cmd.CheckoutDirectCmd.OrderLine(
                            sku, 2)),
                            "A",
                            coupon.id()));
                    return true;
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            },
                    () -> {
                try {
                    placement.createDirect(
                            new CheckoutDirectCmd(
                            1L,
                            List.of(
                            new com.lemnt.mall.application.checkout.cmd.CheckoutDirectCmd.OrderLine(
                            sku, 2)),
                            "B",
                            coupon.id()));
                    return true;
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            });
            long successes = 0;
            for (var result : pool.invokeAll(tasks)) {
                if (result.get()) {
                    successes++;
                }
            }
            assertEquals(1, successes);
            assertEquals(1, count("trade_order"));
        } finally {
            pool.shutdownNow();
        }
    }

    OrderSnapshot paidOrder(Long coupon) {
        var order = place("FULFILL", coupon);
        worker().execute(50);
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        notify.execute(
                new VerifiedPaymentNotification(
                payment.paymentNo(),
                order.orderNo(),
                "ALIPAY",
                "CH-F",
                order.totalAmount(),
                "N-F"));
        return orderRpc.findByOrderNo(order.orderNo());
    }

    ShipmentCreateCmd shipment(String no) {
        return new ShipmentCreateCmd(no, "SHIP-1", "SF", "TRACK-1", "测试收件人", "13800000000", "测试地址");
    }

    @Test
    void shippingRequiresAllStockConfirmedAndReceiptReplaysKeepOriginalTimes() throws Exception {
        var order = paidOrder(null);
        assertFalse(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
        assertThrows(
                IllegalStateException.class,
                () -> fulfillmentService.ship(shipment(order.orderNo())));
        assertEquals(0, count("trade_shipment"));
        assertEquals(1, worker().execute(50));
        assertTrue(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
        var shipped = fulfillmentService.ship(shipment(order.orderNo()));
        assertEquals(shipped, fulfillmentService.ship(shipment(order.orderNo())));
        assertEquals("SHIPPED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertThrows(
                IllegalStateException.class,
                () ->
                fulfillmentService.ship(
                new ShipmentCreateCmd(
                order.orderNo(),
                "SHIP-1",
                "SF",
                "DIFFERENT",
                "测试收件人",
                "13800000000",
                "测试地址")));
        assertThrows(
                IllegalStateException.class,
                () ->
                fulfillmentService.requestRefund(
                new RefundRequestCmd(order.orderNo(), "R1", "申请")));
        var received = fulfillmentService.receive(order.orderNo());
        assertNotNull(received.receivedAt());
        assertEquals(received, fulfillmentService.receive(order.orderNo()));
        assertEquals(received, fulfillmentService.ship(shipment(order.orderNo())));
        assertEquals("COMPLETED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals(8, productGateway.findById(sku).stock());
    }

    @Test
    void refundRequestCopiesDiscountedPaymentAndBlocksShippingUntilRejected() throws Exception {
        var coupon = checkoutService.issue(1L, campaign(10, new BigDecimal("3.00")).id());
        var order = paidOrder(coupon.id());
        worker().execute(50);
        var cmd = new RefundRequestCmd(order.orderNo(), "R1", "不再需要");
        var request = fulfillmentService.requestRefund(cmd);
        assertEquals(new BigDecimal("17.00"), request.amount());
        assertEquals("REQUESTED", request.status());
        assertEquals(request, fulfillmentService.requestRefund(cmd));
        assertTrue(refundRequestGateway.activeRefund(order.orderNo()));
        assertThrows(
                IllegalStateException.class,
                () ->
                fulfillmentService.requestRefund(
                new RefundRequestCmd(order.orderNo(), "R1", "不同原因")));
        assertThrows(
                IllegalStateException.class,
                () ->
                fulfillmentService.requestRefund(
                new RefundRequestCmd(order.orderNo(), "R2", "第二申请")));
        assertThrows(
                IllegalStateException.class,
                () -> fulfillmentService.ship(shipment(order.orderNo())));
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals("SUCCESS", payments.findByPaymentNo(request.paymentNo()).status());
        assertEquals("USED", couponGateway.coupon(1L, coupon.id(), false).status());
        assertEquals(8, productGateway.findById(sku).stock());
        var rejected =
        fulfillmentService.rejectRefund(new RefundRejectCmd(request.refundNo(), "核验后驳回"));
        assertEquals("REJECTED", rejected.status());
        assertEquals(
                rejected,
                fulfillmentService.rejectRefund(new RefundRejectCmd(request.refundNo(), "核验后驳回")));
        assertEquals(rejected, fulfillmentService.requestRefund(cmd));
        assertFalse(refundRequestGateway.activeRefund(order.orderNo()));
        assertThrows(
                IllegalStateException.class,
                () ->
                fulfillmentService.rejectRefund(
                new RefundRejectCmd(request.refundNo(), "改写审核")));
        fulfillmentService.ship(shipment(order.orderNo()));
    }

    @Test
    void rejectedRequestAllowsNewKeyButNeverReopensOriginalRequest() throws Exception {
        var order = paidOrder(null);
        var first =
        fulfillmentService.requestRefund(new RefundRequestCmd(order.orderNo(), "R1", "原因"));
        fulfillmentService.rejectRefund(new RefundRejectCmd(first.refundNo(), "驳回"));
        var second =
        fulfillmentService.requestRefund(
                new RefundRequestCmd(order.orderNo(), "R2", "补充材料"));
        assertNotEquals(first.refundNo(), second.refundNo());
        assertEquals("REQUESTED", second.status());
        assertEquals(2, count("trade_refund_request"));
    }

    @Test
    void refundRequiresExactlyOneMatchingSuccessfulPayment() throws Exception {
        var order = paidOrder(null);
        sql(
                tradeSource,
                "UPDATE trade_payment SET amount=1.00 WHERE order_no='" + order.orderNo() + "'");
        assertThrows(
                IllegalStateException.class,
                () ->
                fulfillmentService.requestRefund(
                new RefundRequestCmd(order.orderNo(), "R1", "原因")));
        sql(
                tradeSource,
                "UPDATE trade_payment SET amount=20.00 WHERE order_no='" + order.orderNo() + "'");
        sql(
                tradeSource,
                "INSERT INTO trade_payment(payment_no,order_no,channel,amount,status)"
                + " VALUES('DUP','"
                + order.orderNo()
                + "','WECHAT',20.00,'SUCCESS')");
        assertThrows(
                IllegalStateException.class,
                () ->
                fulfillmentService.requestRefund(
                new RefundRequestCmd(order.orderNo(), "R1", "原因")));
        assertEquals(0, count("trade_refund_request"));
    }

    @Test
    void concurrentShipmentAndRefundSerializeOnOrderAndCannotBothWin() throws Exception {
        var order = paidOrder(null);
        worker().execute(50);
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> ship =
            () -> {
                start.await();
                try {
                    fulfillmentService.ship(shipment(order.orderNo()));
                    return true;
                } catch (IllegalStateException ex) {
                    return false;
                }
            };
            Callable<Boolean> refund =
            () -> {
                start.await();
                try {
                    fulfillmentService.requestRefund(
                            new RefundRequestCmd(order.orderNo(), "R1", "原因"));
                    return true;
                } catch (IllegalStateException ex) {
                    return false;
                }
            };
            var a = pool.submit(ship);
            var b = pool.submit(refund);
            start.countDown();
            assertNotEquals(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
            assertNotEquals(
                    shipmentGateway.shipment(order.orderNo()) != null,
                    refundRequestGateway.activeRefund(order.orderNo()));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void paidOutboxRetryAfterLostAcknowledgementStillWorksAfterShipping() throws Exception {
        var order = paidOrder(null);
        TradeOutboxGateway lostAck =
        new TradeOutboxGateway() {
            public void append(
                    String id, String type, String no, String event, String payload) {
                outbox.append(id, type, no, event, payload);
            }

            public List<OutboxEvent> pending(int limit) {
                return outbox.pending(limit);
            }

            public boolean claim(String id, String token) {
                return outbox.claim(id, token);
            }

            public boolean delivered(String id, String token) {
                return false;
            }

            public boolean failed(String id, String token, int attempt) {
                return outbox.failed(id, token, attempt);
            }

            public boolean replayDead(String id) {
                return outbox.replayDead(id);
            }
        };
        assertEquals(0, new TradeOutboxDispatchCmdExe(tradeComponents.get(OrderPersistenceService.class), lostAck, bridge, benefits, lifecycle).execute(50));
        assertTrue(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
        fulfillmentService.ship(shipment(order.orderNo()));
        sql(
                tradeSource,
                "UPDATE trade_outbox SET lease_until=TIMESTAMP '2000-01-01 00:00:00' WHERE"
                + " status='PROCESSING'");
        assertEquals(1, worker().execute(50));
        assertEquals("SHIPPED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals(8, productGateway.findById(sku).stock());
        assertEquals(1, count("trade_inventory_confirmation"));
    }

    @Test
    void readQueriesExposeAccuratePagingAndValidation() {
        var order = paidOrder(null);
        worker().execute(50);
        var request =
        fulfillmentService.requestRefund(new RefundRequestCmd(order.orderNo(), "R1", "原因"));
        var pages =
        new OrderPageQueryExe(tradeComponents.get(TradeOrderGateway.class));
        var page =
        pages.execute(
                new OrderPageQry(
                order.orderNo(), "PAID", 1, 20));
        assertEquals(1, page.total());
        assertTrue(page.records().get(0).activeRefund());
        assertTrue(page.records().get(0).inventoryConfirmed());
        var refunds =
        new RefundPageQueryExe(tradeComponents.get(RefundRequestGateway.class));
        assertEquals(
                request.refundNo(),
                refunds.execute(
                new RefundPageQry(
                order.orderNo(), "REQUESTED", 1, 20))
                .records()
                .get(0)
                .refundNo());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                pages.execute(
                new OrderPageQry(
                null, "REFUNDED", 1, 20)));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                refunds.execute(
                new RefundPageQry(
                null, null, 0, 20)));
        assertThrows(
                IllegalArgumentException.class, () -> fulfillmentService.receive(order.orderNo()));
    }

    @Test
    void ambiguousStockConfirmCannotUnlockShipmentUntilRetryReturnsSuccess() throws Exception {
        var order = paidOrder(null);
        loseNextConfirmResponse = true;
        assertEquals(0, worker().execute(50));
        assertFalse(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
        assertThrows(
                IllegalStateException.class,
                () -> fulfillmentService.ship(shipment(order.orderNo())));
        sql(
                tradeSource,
                "UPDATE trade_outbox SET next_attempt_at=TIMESTAMP '2000-01-01 00:00:00' WHERE"
                + " status='FAILED'");
        assertEquals(1, worker().execute(50));
        assertTrue(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
        fulfillmentService.ship(shipment(order.orderNo()));
        assertEquals(8, productGateway.findById(sku).stock());
    }

    @Test
    void shipmentAndReceiveRecordsRollbackWhenOrderTransitionFails() {
        var order = paidOrder(null);
        worker().execute(50);
        var failure =
        Mockito.mock(
                TradeOrderGateway.class, AdditionalAnswers.delegatesTo(tradeOrderGateway));
        Mockito.doReturn(false)
            .when(failure)
            .transitionStatus(order.orderNo(), "PAID", "SHIPPED");
        var failing = tx(new OrderFulfillmentService(tradeComponents.get(InventoryConfirmationGateway.class), tradeComponents.get(OrderPersistenceService.class), tradeComponents.get(RefundRequestGateway.class), tradeComponents.get(ShipmentGateway.class), failure, payments), tradeSource);
        assertThrows(IllegalStateException.class, () -> failing.ship(shipment(order.orderNo())));
        assertNull(shipmentGateway.shipment(order.orderNo()));
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        fulfillmentService.ship(shipment(order.orderNo()));
        Mockito.doReturn(false)
            .when(failure)
            .transitionStatus(order.orderNo(), "SHIPPED", "COMPLETED");
        assertThrows(IllegalStateException.class, () -> failing.receive(order.orderNo()));
        assertNull(shipmentGateway.shipment(order.orderNo()).receivedAt());
        assertEquals("SHIPPED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void v5BackfillOnlyTrustsDeliveredPaidEvents() throws Exception {
        var delivered = paidOrder(null);
        worker().execute(50);
        checkoutService.saveCart(1L, sku, 1, true);
        var unconfirmed = place("SECOND-UNCONFIRMED", null);
        worker().execute(50);
        var payment = paymentCreate.execute(unconfirmed.orderNo(), "ALIPAY");
        notify.execute(
                new VerifiedPaymentNotification(
                payment.paymentNo(),
                unconfirmed.orderNo(),
                "ALIPAY",
                "CH-SECOND",
                unconfirmed.totalAmount(),
                "N-SECOND"));
        assertTrue(inventoryConfirmationGateway.inventoryConfirmed(delivered.orderNo()));
        assertFalse(inventoryConfirmationGateway.inventoryConfirmed(unconfirmed.orderNo()));
        sql(tradeSource, "DROP TABLE trade_refund_request");
        sql(tradeSource, "DROP TABLE trade_shipment");
        sql(tradeSource, "DROP TABLE trade_inventory_confirmation");
        ScriptUtils.executeSqlScript(
                tradeSchema, new ClassPathResource("trade-sql/V5__order_fulfillment.sql"));
        assertTrue(inventoryConfirmationGateway.inventoryConfirmed(delivered.orderNo()));
        assertFalse(inventoryConfirmationGateway.inventoryConfirmed(unconfirmed.orderNo()));
        assertEquals(1, count("trade_inventory_confirmation"));
    }

    @Test
    void consumerReceiptChecksOwnerAgainstPersistedOrderAndCheckout() {
        var order = place("APP-OWNER", null);
        var query =
        new CheckoutReceiptQueryExe(mallComponents.get(CheckoutService.class), orderRpc);
        assertEquals(order.orderNo(), query.execute(1L, order.orderNo()).orderNo());
        assertNull(query.execute(2L, order.orderNo()));
        assertNull(query.execute(1L, "MISSING"));
    }

    Settlement settlement() {
        var value = placement.prepareSettlement(new CheckoutQuoteQry(1L, List.of(sku), null));
        settlements.cache(value);
        return value;
    }

    /** 报价仅缓存；订单已提交但响应丢失时，同一请求只能产生一张订单。 */
    @Test
    void redisSettlementSubmissionRetriesAfterLostOrderResponse() throws Exception {
        var coupon = checkoutService.issue(1L, campaign(10, BigDecimal.ONE).id());
        var value = placement.prepareSettlement(new CheckoutQuoteQry(1L, List.of(sku), coupon.id()));
        settlements.cache(value);
        assertEquals(0, count("trade_order"));
        assertEquals(0, count("mall_checkout"));
        loseNextOrderResponse = true;
        assertThrows(IllegalStateException.class,
                () -> placement.submitSettlement(1L, value.id(), "LOST-ORDER-ACK", settlements));
        assertEquals(1, count("trade_order"));
        assertEquals(1, count("trade_outbox"));
        settlementCache.clear();
        var order = placement.submitSettlement(1L, value.id(), "LOST-ORDER-ACK", settlements);
        assertEquals(order.orderNo(), placement.submitSettlement(
                1L, value.id(), "LOST-ORDER-ACK", settlements).orderNo());
        assertEquals(1, count("trade_order"));
        assertEquals("LOCKED", couponGateway.coupon(1L, coupon.id(), false).status());
        worker().execute(50);
        assertTrue(cartGateway.cart(1L).isEmpty());
    }

    @Test
    void settlementRetrySurvivesCacheEvictionAndRejectsAnotherRequest() throws Exception {
        var value = settlement();
        var order = placement.submitSettlement(1L, value.id(), "S1", settlements);
        settlementCache.clear();
        assertEquals(
                order.orderNo(),
                placement.submitSettlement(1L, value.id(), "S1", settlements).orderNo());
        assertThrows(
                IllegalStateException.class,
                () -> placement.submitSettlement(1L, value.id(), "S2", settlements));
        assertEquals(1, count("trade_order"));
        assertEquals(1, count("mall_settlement_order"));
    }

    @Test
    void settlementRequiresOwnerAndUnexpiredCache() throws Exception {
        var value = settlement();
        assertThrows(
                IllegalArgumentException.class,
                () -> placement.submitSettlement(2L, value.id(), "S1", settlements));
        settlementCache.put(
                value.id(),
                new Settlement(
                value.id(),
                1L,
                value.quote(),
                value.references(),
                Instant.now().minusSeconds(1)));
        assertThrows(
                IllegalArgumentException.class,
                () -> placement.submitSettlement(1L, value.id(), "S1", settlements));
        assertEquals(0, count("trade_order"));
    }

    @Test
    void settlementUsesCachedQuantityAndPreservesLaterCartEdits() {
        var value = settlement();
        checkoutService.saveCart(1L, sku, 3, true);
        var order = placement.submitSettlement(1L, value.id(), "S1", settlements);
        assertEquals(2, orderPersistenceService.findByOrderNo(order.orderNo()).items().get(0).quantity());
        worker().execute(50);
        assertEquals(3, cartGateway.cart(1L).get(0).quantity());
    }

    @Test
    void settlementRejectsChangedPriceWithoutMutatingSnapshotOrOrder() throws Exception {
        var value = settlement();
        sql(mallSource, "UPDATE mall_product SET price=99.00 WHERE id=" + sku);
        assertThrows(
                IllegalStateException.class,
                () -> placement.submitSettlement(1L, value.id(), "S1", settlements));
        assertEquals(
                new BigDecimal("20.00"), settlementCache.get(value.id()).quote().payableAmount());
        assertEquals(0, count("trade_order"));
        assertEquals(0, count("mall_settlement_order"));
    }

    @Test
    void concurrentDifferentKeysCannotConsumeSettlementTwice() throws Exception {
        var value = settlement();
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            Function<String, Boolean> submit =
            key -> {
                try {
                    placement.submitSettlement(1L, value.id(), key, settlements);
                    return true;
                } catch (IllegalStateException ex) {
                    return false;
                }
            };
            var a =
            pool.submit(
                    () -> {
                start.await();
                return submit.apply("A");
            });
            var b =
            pool.submit(
                    () -> {
                start.await();
                return submit.apply("B");
            });
            start.countDown();
            assertNotEquals(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
            assertEquals(1, count("trade_order"));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void settlementBindingFailureRollsBackOrderCheckoutAndOutbox() throws Exception {
        var value = settlement();
        var failing = Mockito.mock(com.lemnt.mall.domain.checkout.gateway.SettlementOrderGateway.class,
                AdditionalAnswers.delegatesTo(settlementOrderGateway));
        Mockito.doThrow(new IllegalStateException("bind failed"))
            .when(failing)
            .bind(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString());
        var failedPlacement = new OrderPlacementService(tx(new com.lemnt.mall.application.checkout.service.CheckoutPreparationService(mallComponents.get(CartGateway.class), failing, settlementAbandonGateway, mallComponents.get(CheckoutAccountGateway.class), mallComponents.get(CheckoutCartGateway.class), mallComponents.get(CheckoutService.class), mallComponents.get(CouponGateway.class), mallComponents.get(CouponTemplateGateway.class), orderRpc, settlements, bridge, new CheckoutMemberService(id -> new MemberSnapshot(id, 1))), mallSource), orderRpc);
        assertThrows(IllegalStateException.class, () -> failedPlacement.submitSettlement(1L, value.id(), "S1", settlements));
        assertEquals(0, count("trade_order"));
        assertEquals(0, count("mall_checkout"));
        assertEquals(0, count("trade_outbox"));
        assertNotNull(settlementCache.get(value.id()));
        assertNotNull(placement.submitSettlement(1L, value.id(), "S1", settlements));
    }

    @Test
    void cachedRequestRecoveryIsOwnerScopedAndIndependentOfCache() {
        var value = settlement();
        var order = placement.submitSettlement(1L, value.id(), "RECOVER", settlements);
        settlementCache.clear();
        var cache =
        Mockito.mock(
                SettlementGateway.class);
        var service =
        new SettlementService(mallComponents.get(CheckoutService.class), orderRpc, placement, cache);
        assertEquals(order.orderNo(), service.findByRequest(1L, "RECOVER").orderNo());
        assertNull(service.findByRequest(2L, "RECOVER"));
        assertNull(service.findByRequest(1L, "MISSING"));
        Mockito.verifyNoInteractions(cache);
    }

    @Test
    void legacyRequestRecoveryDoesNotCreateAnotherOrder() throws Exception {
        var order = place("LEGACY-RECOVER", null);
        var service =
        new SettlementService(mallComponents.get(CheckoutService.class), orderRpc, placement, settlements);
        assertEquals(order.orderNo(), service.findByRequest(1L, "LEGACY-RECOVER").orderNo());
        assertEquals(1, count("trade_order"));
    }

    @Test
    void differentSettlementsCannotReuseConsumedCartVersionAfterReservationCompletes()
    throws Exception {
        var first = settlement();
        var second = settlement();
        placement.submitSettlement(1L, first.id(), "VERSION-A", settlements);
        worker().execute(50);
        assertTrue(cartGateway.cart(1L).isEmpty());
        assertThrows(
                IllegalStateException.class,
                () -> placement.submitSettlement(1L, second.id(), "VERSION-B", settlements));
        assertEquals(1, count("trade_order"));
    }

    @Test
    void editedCartDoesNotMakeOldVersionReusableButNewPreviewCanPurchase() throws Exception {
        var first = settlement();
        var second = settlement();
        checkoutService.saveCart(1L, sku, 3, true);
        placement.submitSettlement(1L, first.id(), "VERSION-A", settlements);
        worker().execute(50);
        assertEquals(3, cartGateway.cart(1L).get(0).quantity());
        assertThrows(
                IllegalStateException.class,
                () -> placement.submitSettlement(1L, second.id(), "VERSION-B", settlements));
        var fresh = settlement();
        assertNotNull(placement.submitSettlement(1L, fresh.id(), "VERSION-C", settlements));
        assertEquals(2, count("trade_order"));
    }

    @Test
    void closedOrderAllowsUnconsumedAlternateSettlementToRetry() {
        var first = settlement();
        var second = settlement();
        var order = placement.submitSettlement(1L, first.id(), "VERSION-A", settlements);
        assertTrue(closer.execute(order.orderNo()));
        worker().execute(50);
        assertNotNull(placement.submitSettlement(1L, second.id(), "VERSION-B", settlements));
    }

    @Test
    void abandonBeforeSubmitFencesAllDelayedRequestKeysAndReplays() throws Exception {
        var value = settlement();
        assertNull(placement.abandonSettlement(1L, value.id(), "A", settlements));
        assertNull(placement.abandonSettlement(1L, value.id(), "A", settlements));
        assertThrows(
                IllegalStateException.class,
                () -> placement.submitSettlement(1L, value.id(), "A", settlements));
        assertThrows(
                IllegalStateException.class,
                () -> placement.submitSettlement(1L, value.id(), "B", settlements));
        assertEquals(0, count("trade_order"));
        assertEquals(1, count("mall_settlement_abandon"));
    }

    @Test
    void abandonAfterSubmitReturnsOriginalOrderWithoutClosingIt() throws Exception {
        var value = settlement();
        var order = placement.submitSettlement(1L, value.id(), "A", settlements);
        settlementCache.clear();
        assertEquals(
                order.orderNo(),
                placement.abandonSettlement(1L, value.id(), "A", settlements).orderNo());
        assertEquals(
                order.orderNo(),
                placement.abandonSettlement(1L, value.id(), "B", settlements).orderNo());
        assertEquals("RESERVING", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals(0, count("mall_settlement_abandon"));
    }

    @Test
    void abandonMarkerIsScopedToMemberAndCannotFenceAnotherOwner() {
        var value = settlement();
        assertNull(placement.abandonSettlement(2L, value.id(), "A", settlements));
        assertFalse(settlementAbandonGateway.abandoned(1L, value.id()));
        assertNotNull(placement.submitSettlement(1L, value.id(), "A", settlements));
    }

    @Test
    void concurrentSubmitAndAbandonHaveOneDurableOutcome() throws Exception {
        var value = settlement();
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            var submit =
            pool.submit(
                    () -> {
                start.await();
                try {
                    return placement.submitSettlement(
                            1L, value.id(), "A", settlements);
                } catch (IllegalStateException ex) {
                    return null;
                }
            });
            var abandon =
            pool.submit(
                    () -> {
                start.await();
                return placement.abandonSettlement(
                        1L, value.id(), "A", settlements);
            });
            start.countDown();
            var order = submit.get(15, TimeUnit.SECONDS);
            var result = abandon.get(15, TimeUnit.SECONDS);
            if (order == null) {
                assertNull(result);
                assertEquals(0, count("trade_order"));
                assertEquals(1, count("mall_settlement_abandon"));
            } else {
                assertNotNull(result);
                assertEquals(order.orderNo(), result.orderNo());
                assertEquals(1, count("trade_order"));
                assertEquals(0, count("mall_settlement_abandon"));
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void abandonWorksWithoutRedisAccess() {
        var value = settlement();
        var gateway =
        Mockito.mock(
                SettlementGateway.class,
                AdditionalAnswers.delegatesTo(settlements));
        Mockito.doThrow(new IllegalStateException("Redis down"))
            .when(gateway)
            .cached(ArgumentMatchers.anyString());
        assertNull(placement.abandonSettlement(1L, value.id(), "A", gateway));
        Mockito.verify(gateway, Mockito.never())
            .cached(ArgumentMatchers.anyString());
    }

    @Test
    void abandonWriteFailureRollsBackMarkerAndAllowsOriginalSubmit() {
        var value = settlement();
        var gateway = Mockito.mock(com.lemnt.mall.domain.checkout.gateway.SettlementAbandonGateway.class,
                AdditionalAnswers.delegatesTo(settlementAbandonGateway));
        Mockito.doAnswer(
                call -> {
            settlementAbandonGateway.abandon(call.getArgument(0), call.getArgument(1));
            throw new IllegalStateException("commit failure");
        })
            .when(gateway)
            .abandon(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString());
        assertThrows(
                IllegalStateException.class,
                () -> placementWith(settlementOrderGateway, gateway).abandonSettlement(1L, value.id(), "A", settlements));
        assertFalse(settlementAbandonGateway.abandoned(1L, value.id()));
        assertNotNull(placement.submitSettlement(1L, value.id(), "A", settlements));
    }

    @Test
    void memberPaymentStatusReflectsVerifiedLocalPaymentAcrossFulfillment() {
        var order = paidOrder(null);
        var query =
        new MemberPaymentStatusQueryExe(tradeComponents.get(OrderPersistenceService.class), payments);
        var key =
        new MemberPaymentStatusQry(
                1L, order.orderNo());
        assertEquals("SUCCESS", query.execute(key).localPaymentStatus());
        assertFalse(query.execute(key).channelVerified());
        worker().execute(50);
        fulfillmentService.ship(shipment(order.orderNo()));
        assertEquals("SHIPPED", query.execute(key).orderStatus());
        fulfillmentService.receive(order.orderNo());
        assertEquals("COMPLETED", query.execute(key).orderStatus());
        assertEquals("SUCCESS", query.execute(key).localPaymentStatus());
        assertNull(
                query.execute(
                new MemberPaymentStatusQry(
                2L, order.orderNo())));
    }

    @Test
    void inconsistentPersistedPaymentIsReportedWithoutChangingOrderOrInventory() throws Exception {
        var order = paidOrder(null);
        var stock = productGateway.findById(sku).stock();
        sql(
                tradeSource,
                "UPDATE trade_payment SET amount=0.01 WHERE order_no='" + order.orderNo() + "'");
        var query =
        new MemberPaymentStatusQueryExe(tradeComponents.get(OrderPersistenceService.class), payments);
        assertEquals(
                "RECONCILE_REQUIRED",
                query.execute(
                new MemberPaymentStatusQry(
                1L, order.orderNo()))
                .localPaymentStatus());
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals(stock, productGateway.findById(sku).stock());
    }

    @Test
    void publicClaimDefaultsClosedAndDoesNotExposePrivateTemplates() {
        var t = campaign(2, new BigDecimal("2.00"));
        assertThrows(
                IllegalStateException.class,
                () ->
                store.claim(
                new CouponClaimCmd(
                1L, t.id())));
        assertEquals(
                0,
                store.offers(new PromotionPageQry(1L, 1, 20))
                .total());
        store.publicClaim(
                new CouponPublicClaimCmd(t.id(), true));
        assertEquals(
                1,
                store.offers(new PromotionPageQry(1L, 1, 20))
                .total());
        var c = store.claim(new CouponClaimCmd(1L, t.id()));
        assertEquals(
                c.id(),
                store.claim(new CouponClaimCmd(1L, t.id()))
                .id());
        assertEquals(1, couponTemplateGateway.template(t.id()).issuedCount());
        store.publicClaim(
                new CouponPublicClaimCmd(t.id(), false));
        assertEquals(
                c.id(),
                store.claim(new CouponClaimCmd(1L, t.id()))
                .id());
        assertThrows(
                IllegalStateException.class,
                () ->
                store.claim(
                new CouponClaimCmd(
                2L, t.id())));
        var wallet =
        store.wallet(new PromotionPageQry(1L, 1, 20));
        assertEquals(1, wallet.total());
        assertTrue(wallet.records().get(0).usable());
        assertFalse(wallet.records().get(0).claimable());
        assertEquals(
                0,
                store.wallet(new PromotionPageQry(2L, 1, 20))
                .total());
    }

    @Test
    void publicClaimConcurrentQuotaHasExactlyOneWinner() throws Exception {
        var t = campaign(1, BigDecimal.ONE);
        store.publicClaim(
                new CouponPublicClaimCmd(t.id(), true));
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            var tasks = new ArrayList<Future<Boolean>>();
            for (long member : List.of(1L, 2L)) {
                tasks.add(
                        pool.submit(
                        () -> {
                    start.await();
                    try {
                        store.claim(
                                new com.lemnt.mall.application.store.cmd
                                .CouponClaimCmd(member, t.id()));
                        return true;
                    } catch (IllegalStateException ex) {
                        return false;
                    }
                }));
            }
            start.countDown();
            assertNotEquals(
                    tasks.get(0).get(10, TimeUnit.SECONDS), tasks.get(1).get(10, TimeUnit.SECONDS));
            assertEquals(1, couponTemplateGateway.template(t.id()).issuedCount());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void expiredAndDisabledCouponsStayInWalletButCannotBeClaimedOrUsed() throws Exception {
        var t = campaign(3, BigDecimal.ONE);
        store.publicClaim(
                new CouponPublicClaimCmd(t.id(), true));
        store.claim(new CouponClaimCmd(1L, t.id()));
        couponTemplateGateway.enableTemplate(t.id(), false);
        assertEquals(
                0,
                store.offers(new PromotionPageQry(1L, 1, 20))
                .total());
        assertFalse(
                store.wallet(new PromotionPageQry(1L, 1, 20))
                .records()
                .get(0)
                .usable());
        couponTemplateGateway.enableTemplate(t.id(), true);
        try (var st = mallSchema.createStatement()) {
            st.execute(
                    "UPDATE mall_coupon_template SET"
                    + " valid_until=DATEADD('MINUTE',-1,CURRENT_TIMESTAMP) WHERE id="
                    + t.id());
        }
        assertThrows(
                IllegalStateException.class,
                () ->
                store.claim(
                new CouponClaimCmd(
                2L, t.id())));
        assertFalse(
                store.wallet(new PromotionPageQry(1L, 1, 20))
                .records()
                .get(0)
                .usable());
    }

    @Test
    void cartAdditionIsIncrementalIdempotentAndDoesNotResurrectConsumedRows() {
        var cmd = new CartAddCmd(1L, sku, 3, "ADD-1");
        assertEquals(5, store.add(cmd).resultingQuantity());
        assertEquals(5, store.add(cmd).resultingQuantity());
        assertEquals(5, cartGateway.cart(1L).get(0).quantity());
        assertThrows(
                IllegalStateException.class,
                () ->
                store.add(
                new CartAddCmd(
                1L, sku, 2, "ADD-1")));
        checkoutService.deleteCart(1L, sku);
        assertEquals(5, store.add(cmd).resultingQuantity());
        assertTrue(cartGateway.cart(1L).isEmpty());
        assertEquals(
                3,
                store.add(new CartAddCmd(2L, sku, 3, "ADD-1"))
                .resultingQuantity());
    }

    @Test
    void additionLedgerFailureRollsBackIncrementAndRetry() throws Exception {
        try (var st = mallSchema.createStatement()) {
            st.execute(
                    "ALTER TABLE mall_cart_add_request ADD CONSTRAINT reject_add"
                    + " CHECK(quantity<3)");
        }
        var cmd = new CartAddCmd(1L, sku, 3, "ADD-ROLLBACK");
        assertThrows(RuntimeException.class, () -> store.add(cmd));
        assertEquals(2, cartGateway.cart(1L).get(0).quantity());
        try (var st = mallSchema.createStatement()) {
            st.execute("ALTER TABLE mall_cart_add_request DROP CONSTRAINT reject_add");
        }
        assertEquals(5, store.add(cmd).resultingQuantity());
        assertThrows(
                IllegalStateException.class,
                () ->
                store.add(
                new CartAddCmd(
                1L, sku, 6, "ADD-OVER")));
        assertEquals(5, cartGateway.cart(1L).get(0).quantity());
    }

    @Test
    void concurrentSameAdditionHasOneIncrement() throws Exception {
        var cmd = new CartAddCmd(1L, sku, 3, "ADD-RACE");
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            Callable<Integer> task =
            () -> {
                start.await();
                return store.add(cmd).resultingQuantity();
            };
            var a = pool.submit(task);
            var b = pool.submit(task);
            start.countDown();
            assertEquals(5, a.get(10, TimeUnit.SECONDS));
            assertEquals(5, b.get(10, TimeUnit.SECONDS));
            assertEquals(5, cartGateway.cart(1L).get(0).quantity());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void couponInsertFailureRollsBackQuotaThenRetrySucceeds() throws Exception {
        var t = campaign(3, BigDecimal.ONE);
        store.publicClaim(
                new CouponPublicClaimCmd(t.id(), true));
        try (var st = mallSchema.createStatement()) {
            st.execute("ALTER TABLE mall_coupon ADD CONSTRAINT reject_claim CHECK(member_id<2)");
        }
        var c = new CouponClaimCmd(2L, t.id());
        assertThrows(RuntimeException.class, () -> store.claim(c));
        assertEquals(0, couponTemplateGateway.template(t.id()).issuedCount());
        try (var st = mallSchema.createStatement()) {
            st.execute("ALTER TABLE mall_coupon DROP CONSTRAINT reject_claim");
        }
        assertEquals(2L, store.claim(c).memberId());
        assertEquals(1, couponTemplateGateway.template(t.id()).issuedCount());
    }

    @Test
    void distinctConcurrentAdditionsAreAccumulated() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            var a =
            pool.submit(
                    () -> {
                start.await();
                return store.add(
                        new CartAddCmd(
                        1L, sku, 2, "DISTINCT-1"));
            });
            var b =
            pool.submit(
                    () -> {
                start.await();
                return store.add(
                        new CartAddCmd(
                        1L, sku, 3, "DISTINCT-2"));
            });
            start.countDown();
            a.get(10, TimeUnit.SECONDS);
            b.get(10, TimeUnit.SECONDS);
            assertEquals(7, cartGateway.cart(1L).get(0).quantity());
        } finally {
            pool.shutdownNow();
        }
    }
    private OrderPlacementService placementWith(
            com.lemnt.mall.domain.checkout.gateway.SettlementOrderGateway orderGateway,
            com.lemnt.mall.domain.checkout.gateway.SettlementAbandonGateway abandonGateway) {
        return new OrderPlacementService(tx(new com.lemnt.mall.application.checkout.service.CheckoutPreparationService(mallComponents.get(CartGateway.class), orderGateway, abandonGateway, mallComponents.get(CheckoutAccountGateway.class), mallComponents.get(CheckoutCartGateway.class), mallComponents.get(CheckoutService.class), mallComponents.get(CouponGateway.class), mallComponents.get(CouponTemplateGateway.class), orderRpc, settlements, bridge, new CheckoutMemberService(id -> new MemberSnapshot(id, 1))), mallSource), orderRpc);
    }
}
