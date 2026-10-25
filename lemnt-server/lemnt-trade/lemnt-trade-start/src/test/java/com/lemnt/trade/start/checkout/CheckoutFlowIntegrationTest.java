package com.lemnt.trade.start.checkout;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.mall.domain.gateway.ProductGateway;
import com.lemnt.mall.infrastructure.gateway.ProductGatewayImpl;
import com.lemnt.mall.infrastructure.mapper.ProductMapper;
import com.lemnt.mall.infrastructure.mapper.StockReservationMapper;
import com.lemnt.trade.application.checkout.cmd.*;
import com.lemnt.trade.application.fulfillment.cmd.*;
import com.lemnt.trade.application.fulfillment.service.OrderFulfillmentService;
import com.lemnt.trade.domain.fulfillment.gateway.FulfillmentGateway;
import com.lemnt.trade.infrastructure.fulfillment.gateway.FulfillmentGatewayImpl;
import com.lemnt.trade.infrastructure.fulfillment.mapper.FulfillmentMapper;
import com.lemnt.trade.application.checkout.qry.CheckoutQuoteQry;
import com.lemnt.trade.application.checkout.service.*;
import com.lemnt.trade.application.executor.*;
import com.lemnt.trade.domain.checkout.gateway.CheckoutGateway;
import com.lemnt.trade.domain.checkout.model.*;
import com.lemnt.trade.domain.gateway.*;
import com.lemnt.trade.domain.model.*;
import com.lemnt.trade.infrastructure.checkout.gateway.CheckoutGatewayImpl;
import com.lemnt.trade.infrastructure.checkout.mapper.CheckoutMapper;
import com.lemnt.trade.infrastructure.gateway.*;
import com.lemnt.trade.infrastructure.mapper.*;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.*;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Two isolated databases, actual production gateways/mappers and transaction proxies. RPC is a test
 * bridge; this is not a Nacos/Dubbo runtime or real payment-channel test.
 */
class CheckoutFlowIntegrationTest {
    JdbcDataSource tradeSource, mallSource;
    Connection tradeSchema, mallSchema;
    CheckoutGateway checkout;
    FulfillmentGateway fulfillment;
    OrderFulfillmentService fulfillmentService;
    TradeOrderGateway orders;
    TradeOutboxGateway outbox;
    TradePaymentGateway payments;
    ProductGateway mall;
    OrderPlacementService placement;
    CheckoutLifecycleService lifecycle;
    TradeOrderCloseCmdExe closer;
    TradePaymentCreateCmdExe paymentCreate;
    PaymentNotifyCmdExe notify;
    ProductCatalogGateway bridge;
    Long sku;
    boolean loseNextReserveResponse;
    boolean loseNextConfirmResponse;
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
        config.setMapUnderscoreToCamelCase(true);
        var bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(source);
        bean.setConfiguration(config);
        bean.setGlobalConfig(new GlobalConfig().setMetaObjectHandler(new MyMetaObjectHandler()));
        var factory = bean.getObject();
        for (var mapper : mappers) config.addMapper(mapper);
        return new SqlSessionTemplate(factory);
    }

    @SuppressWarnings("unchecked")
    <T> T tx(T target, JdbcDataSource source) {
        var proxy = new ProxyFactory(target);
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
                tradeSchema, new ClassPathResource("trade-sql/V4__cart_coupon_checkout.sql"));
        ScriptUtils.executeSqlScript(
                mallSchema, new ClassPathResource("mall-sql/V1__mall_product.sql"));
        ScriptUtils.executeSqlScript(
                mallSchema, new ClassPathResource("mall-sql/V2__mall_catalog.sql"));
        ScriptUtils.executeSqlScript(
                tradeSchema, new ClassPathResource("trade-sql/V5__order_fulfillment.sql"));
        var trade =
                template(
                        tradeSource,
                        CheckoutMapper.class,
                        FulfillmentMapper.class,
                        TradeOrderMapper.class,
                        TradeOrderItemMapper.class,
                        TradeOutboxMapper.class,
                        TradePaymentMapper.class);
        var products = template(mallSource, ProductMapper.class, StockReservationMapper.class);
        checkout = tx(new CheckoutGatewayImpl(trade.getMapper(CheckoutMapper.class)), tradeSource);
        orders =
                tx(
                        new TradeOrderGatewayImpl(
                                trade.getMapper(TradeOrderMapper.class),
                                trade.getMapper(TradeOrderItemMapper.class)),
                        tradeSource);
        outbox =
                tx(
                        new TradeOutboxGatewayImpl(trade.getMapper(TradeOutboxMapper.class)),
                        tradeSource);
        payments =
                tx(
                        new TradePaymentGatewayImpl(trade.getMapper(TradePaymentMapper.class)),
                        tradeSource);
        mall =
                tx(
                        new ProductGatewayImpl(
                                products.getMapper(ProductMapper.class),
                                products.getMapper(StockReservationMapper.class)),
                        mallSource);
        sku =
                mall.save(
                                com.lemnt.mall.domain.model.Product.create(
                                        "SKU1", "sku", null, null, new BigDecimal("10.00"), 10))
                        .id();
        mall.updateStatus(sku, "ON_SHELF");
        bridge =
                new ProductCatalogGateway() {
                    public ProductSnapshot findOnShelf(Long id) {
                        var p = mall.findSellableById(id);
                        return p == null
                                ? null
                                : new ProductSnapshot(
                                        p.id(), p.productCode(), p.name(), p.price(), p.stock());
                    }

                    public Map<Long, ProductSnapshot> findOnShelfBatch(List<Long> ids) {
                        Map<Long, ProductSnapshot> result = new HashMap<>();
                        for (var p : mall.findSellableByIds(ids))
                            result.put(
                                    p.id(),
                                    new ProductSnapshot(
                                            p.id(),
                                            p.productCode(),
                                            p.name(),
                                            p.price(),
                                            p.stock()));
                        return result;
                    }

                    public boolean reserveStock(String no, Long id, Integer quantity) {
                        boolean result = mall.reserveStock(no, id, quantity);
                        if (loseNextReserveResponse) {
                            loseNextReserveResponse = false;
                            throw new IllegalStateException(
                                    "simulated lost reserve response after mall commit");
                        }
                        return result;
                    }

                    public void cancelReservation(String no, Long id, Integer quantity) {
                        mall.cancelReservation(no, id, quantity);
                    }

                    public void releaseStock(String no, Long id) {
                        mall.releaseStock(no, id);
                    }

                    public void confirmStock(String no, Long id) {
                        mall.confirmStock(no, id);
                        if (loseNextConfirmResponse) {
                            loseNextConfirmResponse = false;
                            throw new IllegalStateException("确认库存已提交但RPC响应丢失");
                        }
                    }
                };
        var member = new CheckoutMemberService(id -> new MemberSnapshot(id, 1));
        placement =
                tx(
                        new OrderPlacementService(checkout, orders, outbox, bridge, member),
                        tradeSource);
        fulfillment =
                tx(
                        new FulfillmentGatewayImpl(trade.getMapper(FulfillmentMapper.class)),
                        tradeSource);
        fulfillmentService =
                tx(new OrderFulfillmentService(orders, payments, fulfillment), tradeSource);
        lifecycle =
                tx(
                        new CheckoutLifecycleService(orders, outbox, checkout, fulfillment),
                        tradeSource);
        closer =
                tx(
                        new TradeOrderCloseCmdExe(orders, outbox, payments, p -> false, checkout),
                        tradeSource);
        paymentCreate = tx(new TradePaymentCreateCmdExe(orders, payments), tradeSource);
        notify = tx(new PaymentNotifyCmdExe(payments, orders, outbox, checkout), tradeSource);
        checkout.saveCart(1L, sku, 2, true);
    }

    @AfterEach
    void close() throws Exception {
        if (tradeSchema != null) tradeSchema.close();
        if (mallSchema != null) mallSchema.close();
    }

    TradeOutboxDispatchExe worker() {
        return new TradeOutboxDispatchExe(outbox, orders, bridge, lifecycle);
    }

    CouponTemplate campaign(int quota, BigDecimal amount) {
        var now = checkout.now();
        return checkout.createTemplate(
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

    TradeOrder place(String key, Long coupon) {
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
        try (var c = tradeSource.getConnection();
                var s = c.createStatement();
                var r = s.executeQuery("SELECT COUNT(*) FROM " + table)) {
            r.next();
            return r.getLong(1);
        }
    }

    @Test
    void successfulCheckoutUsesServerPricesLocksCouponAndConsumesCartAfterReservation() {
        var template = campaign(10, new BigDecimal("3.00"));
        var coupon = checkout.issue(1L, template.id());
        var quote = placement.quote(new CheckoutQuoteQry(1L, List.of(sku), coupon.id()));
        assertEquals(new BigDecimal("17.00"), quote.payableAmount());
        var order = place("REQ1", coupon.id());
        assertEquals("RESERVING", order.status());
        assertTrue(
                java.time.Duration.between(order.createTime(), orders.databaseNow())
                                .abs()
                                .getSeconds()
                        < 2);
        assertEquals(10, mall.findById(sku).stock());
        assertThrows(IllegalStateException.class, () -> place("SECOND-KEY", null));
        assertEquals("LOCKED", checkout.coupon(1L, coupon.id(), false).status());
        assertEquals(1, checkout.cart(1L).size());
        assertThrows(
                IllegalStateException.class,
                () -> paymentCreate.execute(order.orderNo(), "ALIPAY"));
        assertEquals(1, worker().execute(50));
        assertEquals("PENDING_PAYMENT", orders.findByOrderNo(order.orderNo()).status());
        assertEquals(8, mall.findById(sku).stock());
        assertTrue(checkout.cart(1L).isEmpty());
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
        assertEquals("USED", checkout.coupon(1L, coupon.id(), false).status());
        assertEquals(1, worker().execute(50));
        assertEquals(8, mall.findById(sku).stock());
    }

    @Test
    void idempotentRetryReturnsOriginalPriceEvenAfterCartDeletionAndCatalogPriceChange()
            throws Exception {
        var order = place("REQ1", null);
        worker().execute(50);
        sql(mallSource, "UPDATE mall_product SET price=99.00 WHERE id=" + sku);
        assertEquals(order.orderNo(), place("REQ1", null).orderNo());
        assertEquals(new BigDecimal("20.00"), checkout.receipt(order.orderNo()).payableAmount());
        assertEquals(1, count("trade_order"));
        assertThrows(IllegalStateException.class, () -> place("REQ1", 99L));
    }

    @Test
    void lostRpcResponseIsRetriedAfterRestartWithoutDoubleDebit() throws Exception {
        var order = place("LOST", null);
        loseNextReserveResponse = true;
        assertEquals(0, worker().execute(50));
        assertEquals(8, mall.findById(sku).stock());
        assertEquals("RESERVING", orders.findByOrderNo(order.orderNo()).status());
        sql(tradeSource, "UPDATE trade_outbox SET next_attempt_at=TIMESTAMP '2000-01-01 00:00:00'");
        assertEquals(1, worker().execute(50));
        assertEquals(8, mall.findById(sku).stock());
        assertEquals("PENDING_PAYMENT", orders.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void closingAfterAmbiguousReserveReleasesCouponAndFencesLateRequests() throws Exception {
        var coupon = checkout.issue(1L, campaign(10, BigDecimal.ONE).id());
        var order = place("LOST-CLOSE", coupon.id());
        loseNextReserveResponse = true;
        worker().execute(50);
        assertTrue(closer.execute(order.orderNo()));
        worker().execute(50);
        assertEquals(10, mall.findById(sku).stock());
        assertEquals("AVAILABLE", checkout.coupon(1L, coupon.id(), false).status());
        assertFalse(mall.reserveStock(order.orderNo(), sku, 2));
        assertEquals(1, checkout.cart(1L).size());
    }

    @Test
    void stockRejectionDurablyClosesAndCancelsUnattemptedProducts() {
        var order = place("REJECT", null);
        mall.updateStatus(sku, "OFF_SHELF");
        assertEquals(1, worker().execute(50));
        assertEquals("CLOSED", orders.findByOrderNo(order.orderNo()).status());
        worker().execute(50);
        mall.updateStatus(sku, "ON_SHELF");
        assertFalse(mall.reserveStock(order.orderNo(), sku, 2));
        assertEquals(10, mall.findById(sku).stock());
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
                                checkout.issue(member, template.id());
                                return true;
                            } catch (IllegalStateException exhausted) {
                                return false;
                            }
                        });
            }
            long successes = 0;
            for (var result : pool.invokeAll(tasks)) if (result.get()) successes++;
            assertEquals(2, successes);
            assertEquals(2, checkout.template(template.id()).issuedCount());
            for (long i = 1; i <= 8; i++)
                if (!checkout.coupons(i).isEmpty()) {
                    assertEquals(
                            checkout.coupons(i).get(0).id(), checkout.issue(i, template.id()).id());
                }
            assertEquals(2, checkout.template(template.id()).issuedCount());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void ownershipSelectedStateAndDisabledTemplatesRejectCheckoutWithoutSideEffects()
            throws Exception {
        var template = campaign(10, BigDecimal.ONE);
        var foreign = checkout.issue(2L, template.id());
        assertThrows(IllegalArgumentException.class, () -> place("FOREIGN", foreign.id()));
        var own = checkout.issue(1L, template.id());
        checkout.enableTemplate(template.id(), false);
        assertThrows(IllegalArgumentException.class, () -> place("DISABLED", own.id()));
        checkout.saveCart(1L, sku, 2, false);
        assertThrows(IllegalArgumentException.class, () -> place("NOT-SELECTED", null));
        assertEquals(0, count("trade_order"));
        assertEquals(0, count("trade_outbox"));
    }

    @Test
    void cartMutationOrDeleteReaddDuringReservationIsNotRemovedByFinalizer() {
        place("V1", null);
        checkout.saveCart(1L, sku, 3, true);
        worker().execute(50);
        assertEquals(3, checkout.cart(1L).get(0).quantity());
        var order = place("V2", null);
        var oldId = checkout.cart(1L).get(0).id();
        checkout.deleteCart(1L, sku);
        checkout.saveCart(1L, sku, 1, true);
        worker().execute(50);
        assertNotEquals(oldId, checkout.cart(1L).get(0).id());
        assertEquals(1, checkout.cart(1L).get(0).quantity());
        assertEquals("PENDING_PAYMENT", orders.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void zeroPayableQuoteIsExplicitlyUnsupportedAtPlacementAndRollsBackLocks() throws Exception {
        var coupon = checkout.issue(1L, campaign(10, new BigDecimal("50.00")).id());
        assertEquals(
                new BigDecimal("0.00"),
                placement
                        .quote(new CheckoutQuoteQry(1L, List.of(sku), coupon.id()))
                        .payableAmount());
        assertThrows(IllegalArgumentException.class, () -> place("ZERO", coupon.id()));
        assertEquals("AVAILABLE", checkout.coupon(1L, coupon.id(), false).status());
        assertEquals(0, count("trade_order"));
    }

    @Test
    void couponInvariantFailureRollsBackPaymentAndOrderTogether() throws Exception {
        var coupon = checkout.issue(1L, campaign(10, BigDecimal.ONE).id());
        var order = place("COUPON-INVARIANT", coupon.id());
        worker().execute(50);
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        sql(
                tradeSource,
                "UPDATE trade_coupon SET status='AVAILABLE',order_no=NULL WHERE id=" + coupon.id());
        var notice =
                new VerifiedPaymentNotification(
                        payment.paymentNo(),
                        order.orderNo(),
                        "ALIPAY",
                        "CH1",
                        order.totalAmount(),
                        "N1");
        assertThrows(IllegalStateException.class, () -> notify.execute(notice));
        assertEquals("WAITING", payments.findByPaymentNo(payment.paymentNo()).status());
        assertEquals("PENDING_PAYMENT", orders.findByOrderNo(order.orderNo()).status());
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
        new com.lemnt.trade.start.TradeOutboxScheduler(worker(), closer, orders).closeExpired();
        assertEquals("CLOSED", orders.findByOrderNo(order.orderNo()).status());
        assertEquals("PENDING_PAYMENT", orders.findByOrderNo("BLOCK0").status());
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
        assertEquals(10, mall.findById(sku).stock());
    }

    @Test
    void unavailableCartItemCanBeDeselectedButNotNewlySelected() {
        mall.updateStatus(sku, "OFF_SHELF");
        var saver =
                tx(
                        new com.lemnt.trade.application.checkout.executor.CartSaveCmdExe(
                                checkout,
                                new CheckoutMemberService(id -> new MemberSnapshot(id, 1)),
                                bridge),
                        tradeSource);
        assertFalse(saver.execute(new CartSaveCmd(1L, sku, 2, false)).selected());
        assertThrows(
                IllegalArgumentException.class,
                () -> saver.execute(new CartSaveCmd(1L, sku, 2, true)));
    }

    @Test
    void failedOutboxInsertRollsBackOrderCouponAndCheckoutBeforeAnyInventoryDebit()
            throws Exception {
        var coupon = checkout.issue(1L, campaign(10, BigDecimal.ONE).id());
        sql(tradeSource, "ALTER TABLE trade_outbox ADD CONSTRAINT test_reject_outbox CHECK(1=0)");
        assertThrows(RuntimeException.class, () -> place("DB-FAIL", coupon.id()));
        assertEquals(0, count("trade_order"));
        assertEquals(0, count("trade_checkout"));
        assertEquals("AVAILABLE", checkout.coupon(1L, coupon.id(), false).status());
        assertEquals(10, mall.findById(sku).stock());
        assertEquals(1, checkout.cart(1L).size());
    }

    @Test
    void expiredCouponCannotBecomeLockedOrCreateOrder() throws Exception {
        var coupon = checkout.issue(1L, campaign(10, BigDecimal.ONE).id());
        sql(
                tradeSource,
                "UPDATE trade_coupon_template SET valid_from=TIMESTAMP '2000-01-01"
                        + " 00:00:00',valid_until=TIMESTAMP '2001-01-01 00:00:00' WHERE id="
                        + coupon.templateId());
        assertThrows(IllegalArgumentException.class, () -> place("EXPIRED", coupon.id()));
        assertEquals(0, count("trade_order"));
        assertEquals("AVAILABLE", checkout.coupon(1L, coupon.id(), false).status());
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
            for (int i = 0; i < 8; i++) tasks.add(() -> placement.checkoutCart(command).orderNo());
            Set<String> orderNos = new HashSet<>();
            for (var result : pool.invokeAll(tasks)) orderNos.add(result.get());
            assertEquals(1, orderNos.size());
            assertEquals(1, count("trade_order"));
            assertEquals(1, count("trade_outbox"));
            assertEquals(10, mall.findById(sku).stock());
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
        assertEquals("PENDING_PAYMENT", orders.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void twoConcurrentRequestsCannotSpendTheSameCouponTwice() throws Exception {
        var coupon = checkout.issue(1L, campaign(10, BigDecimal.ONE).id());
        var pool = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Boolean>> tasks =
                    List.of(
                            () -> {
                                try {
                                    placement.createDirect(
                                            new com.lemnt.trade.application.cmd.TradeOrderCreateCmd(
                                                    1L,
                                                    List.of(
                                                            new com.lemnt.trade.application.cmd
                                                                    .TradeOrderCreateCmd.OrderLine(
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
                                            new com.lemnt.trade.application.cmd.TradeOrderCreateCmd(
                                                    1L,
                                                    List.of(
                                                            new com.lemnt.trade.application.cmd
                                                                    .TradeOrderCreateCmd.OrderLine(
                                                                    sku, 2)),
                                                    "B",
                                                    coupon.id()));
                                    return true;
                                } catch (IllegalArgumentException ex) {
                                    return false;
                                }
                            });
            long successes = 0;
            for (var result : pool.invokeAll(tasks)) if (result.get()) successes++;
            assertEquals(1, successes);
            assertEquals(1, count("trade_order"));
        } finally {
            pool.shutdownNow();
        }
    }

    TradeOrder paidOrder(Long coupon) {
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
        return orders.findByOrderNo(order.orderNo());
    }

    ShipmentCreateCmd shipment(String no) {
        return new ShipmentCreateCmd(no, "SHIP-1", "SF", "TRACK-1", "测试收件人", "13800000000", "测试地址");
    }

    @Test
    void shippingRequiresAllStockConfirmedAndReceiptReplaysKeepOriginalTimes() throws Exception {
        var order = paidOrder(null);
        assertFalse(fulfillment.inventoryConfirmed(order.orderNo()));
        assertThrows(
                IllegalStateException.class,
                () -> fulfillmentService.ship(shipment(order.orderNo())));
        assertEquals(0, count("trade_shipment"));
        assertEquals(1, worker().execute(50));
        assertTrue(fulfillment.inventoryConfirmed(order.orderNo()));
        var shipped = fulfillmentService.ship(shipment(order.orderNo()));
        assertEquals(shipped, fulfillmentService.ship(shipment(order.orderNo())));
        assertEquals("SHIPPED", orders.findByOrderNo(order.orderNo()).status());
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
        assertEquals("COMPLETED", orders.findByOrderNo(order.orderNo()).status());
        assertEquals(8, mall.findById(sku).stock());
    }

    @Test
    void refundRequestCopiesDiscountedPaymentAndBlocksShippingUntilRejected() throws Exception {
        var coupon = checkout.issue(1L, campaign(10, new BigDecimal("3.00")).id());
        var order = paidOrder(coupon.id());
        worker().execute(50);
        var cmd = new RefundRequestCmd(order.orderNo(), "R1", "不再需要");
        var request = fulfillmentService.requestRefund(cmd);
        assertEquals(new BigDecimal("17.00"), request.amount());
        assertEquals("REQUESTED", request.status());
        assertEquals(request, fulfillmentService.requestRefund(cmd));
        assertTrue(fulfillment.activeRefund(order.orderNo()));
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
        assertEquals("PAID", orders.findByOrderNo(order.orderNo()).status());
        assertEquals("SUCCESS", payments.findByPaymentNo(request.paymentNo()).status());
        assertEquals("USED", checkout.coupon(1L, coupon.id(), false).status());
        assertEquals(8, mall.findById(sku).stock());
        var rejected =
                fulfillmentService.rejectRefund(new RefundRejectCmd(request.refundNo(), "核验后驳回"));
        assertEquals("REJECTED", rejected.status());
        assertEquals(
                rejected,
                fulfillmentService.rejectRefund(new RefundRejectCmd(request.refundNo(), "核验后驳回")));
        assertEquals(rejected, fulfillmentService.requestRefund(cmd));
        assertFalse(fulfillment.activeRefund(order.orderNo()));
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
                    fulfillment.shipment(order.orderNo()) != null,
                    fulfillment.activeRefund(order.orderNo()));
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
        assertEquals(0, new TradeOutboxDispatchExe(lostAck, orders, bridge, lifecycle).execute(50));
        assertTrue(fulfillment.inventoryConfirmed(order.orderNo()));
        fulfillmentService.ship(shipment(order.orderNo()));
        sql(
                tradeSource,
                "UPDATE trade_outbox SET lease_until=TIMESTAMP '2000-01-01 00:00:00' WHERE"
                        + " status='PROCESSING'");
        assertEquals(1, worker().execute(50));
        assertEquals("SHIPPED", orders.findByOrderNo(order.orderNo()).status());
        assertEquals(8, mall.findById(sku).stock());
        assertEquals(1, count("trade_inventory_confirmation"));
    }

    @Test
    void readQueriesExposeAccuratePagingAndValidation() {
        var order = paidOrder(null);
        worker().execute(50);
        var request =
                fulfillmentService.requestRefund(new RefundRequestCmd(order.orderNo(), "R1", "原因"));
        var pages =
                new com.lemnt.trade.application.fulfillment.executor.OrderPageQueryExe(fulfillment);
        var page =
                pages.execute(
                        new com.lemnt.trade.application.fulfillment.qry.OrderPageQry(
                                order.orderNo(), "PAID", 1, 20));
        assertEquals(1, page.total());
        assertTrue(page.records().get(0).activeRefund());
        assertTrue(page.records().get(0).inventoryConfirmed());
        var refunds =
                new com.lemnt.trade.application.fulfillment.executor.RefundPageQueryExe(
                        fulfillment);
        assertEquals(
                request.refundNo(),
                refunds.execute(
                                new com.lemnt.trade.application.fulfillment.qry.RefundPageQry(
                                        order.orderNo(), "REQUESTED", 1, 20))
                        .records()
                        .get(0)
                        .refundNo());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        pages.execute(
                                new com.lemnt.trade.application.fulfillment.qry.OrderPageQry(
                                        null, "REFUNDED", 1, 20)));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        refunds.execute(
                                new com.lemnt.trade.application.fulfillment.qry.RefundPageQry(
                                        null, null, 0, 20)));
        assertThrows(
                IllegalArgumentException.class, () -> fulfillmentService.receive(order.orderNo()));
    }

    @Test
    void ambiguousStockConfirmCannotUnlockShipmentUntilRetryReturnsSuccess() throws Exception {
        var order = paidOrder(null);
        loseNextConfirmResponse = true;
        assertEquals(0, worker().execute(50));
        assertFalse(fulfillment.inventoryConfirmed(order.orderNo()));
        assertThrows(
                IllegalStateException.class,
                () -> fulfillmentService.ship(shipment(order.orderNo())));
        sql(
                tradeSource,
                "UPDATE trade_outbox SET next_attempt_at=TIMESTAMP '2000-01-01 00:00:00' WHERE"
                        + " status='FAILED'");
        assertEquals(1, worker().execute(50));
        assertTrue(fulfillment.inventoryConfirmed(order.orderNo()));
        fulfillmentService.ship(shipment(order.orderNo()));
        assertEquals(8, mall.findById(sku).stock());
    }

    @Test
    void shipmentAndReceiveRecordsRollbackWhenOrderTransitionFails() {
        var order = paidOrder(null);
        worker().execute(50);
        var failure =
                org.mockito.Mockito.mock(
                        TradeOrderGateway.class, org.mockito.AdditionalAnswers.delegatesTo(orders));
        org.mockito.Mockito.doReturn(false)
                .when(failure)
                .transitionStatus(order.orderNo(), "PAID", "SHIPPED");
        var failing = tx(new OrderFulfillmentService(failure, payments, fulfillment), tradeSource);
        assertThrows(IllegalStateException.class, () -> failing.ship(shipment(order.orderNo())));
        assertNull(fulfillment.shipment(order.orderNo()));
        assertEquals("PAID", orders.findByOrderNo(order.orderNo()).status());
        fulfillmentService.ship(shipment(order.orderNo()));
        org.mockito.Mockito.doReturn(false)
                .when(failure)
                .transitionStatus(order.orderNo(), "SHIPPED", "COMPLETED");
        assertThrows(IllegalStateException.class, () -> failing.receive(order.orderNo()));
        assertNull(fulfillment.shipment(order.orderNo()).receivedAt());
        assertEquals("SHIPPED", orders.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void v5BackfillOnlyTrustsDeliveredPaidEvents() throws Exception {
        var delivered = paidOrder(null);
        worker().execute(50);
        checkout.saveCart(1L, sku, 1, true);
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
        assertTrue(fulfillment.inventoryConfirmed(delivered.orderNo()));
        assertFalse(fulfillment.inventoryConfirmed(unconfirmed.orderNo()));
        sql(tradeSource, "DROP TABLE trade_refund_request");
        sql(tradeSource, "DROP TABLE trade_shipment");
        sql(tradeSource, "DROP TABLE trade_inventory_confirmation");
        ScriptUtils.executeSqlScript(
                tradeSchema, new ClassPathResource("trade-sql/V5__order_fulfillment.sql"));
        assertTrue(fulfillment.inventoryConfirmed(delivered.orderNo()));
        assertFalse(fulfillment.inventoryConfirmed(unconfirmed.orderNo()));
        assertEquals(1, count("trade_inventory_confirmation"));
    }
}
