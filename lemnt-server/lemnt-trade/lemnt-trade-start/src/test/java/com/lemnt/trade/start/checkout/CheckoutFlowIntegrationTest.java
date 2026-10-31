package com.lemnt.trade.start.checkout;

import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.mall.application.cart.cmd.CartSaveCmd;
import com.lemnt.mall.application.cart.cmd.CartSaveCmd;
import com.lemnt.mall.application.cart.executor.CartSaveCmdExe;
import com.lemnt.mall.application.cart.executor.CartSaveCmdExe;
import com.lemnt.mall.application.cart.service.CartService;
import com.lemnt.mall.application.coupon.cmd.CouponClaimCmd;
import com.lemnt.mall.application.coupon.cmd.CouponPublicClaimCmd;
import com.lemnt.mall.application.coupon.qry.CouponPageQry;
import com.lemnt.mall.application.coupon.service.CouponIssuanceService;
import com.lemnt.mall.application.coupon.service.CouponService;
import com.lemnt.mall.application.dubbo.SettlementLifecycleClientImpl;
import com.lemnt.mall.application.member.service.MemberValidationService;
import com.lemnt.mall.application.product.cmd.CommentCreateCmd;
import com.lemnt.mall.application.product.cmd.CommentReplyCmd;
import com.lemnt.mall.application.product.cmd.CommentVisibleCmd;
import com.lemnt.mall.application.product.executor.CommentPageQueryExe;
import com.lemnt.mall.application.product.executor.CommentVisibleCmdExe;
import com.lemnt.mall.application.product.qry.CommentPageQry;
import com.lemnt.mall.application.product.service.ProductCatalogService;
import com.lemnt.mall.application.product.service.ProductCommentService;
import com.lemnt.mall.application.product.service.ProductInventoryService;
import com.lemnt.mall.application.product.service.ProductManagementService;
import com.lemnt.mall.application.settlement.cmd.SettlementDirectCmd;
import com.lemnt.mall.application.settlement.cmd.SettlementOrderSubmitCmd;
import com.lemnt.mall.application.settlement.executor.SettlementReceiptQueryExe;
import com.lemnt.mall.application.settlement.qry.SettlementQuoteQry;
import com.lemnt.mall.application.settlement.service.OrderPlacementService;
import com.lemnt.mall.application.settlement.service.SettlementPreparationService;
import com.lemnt.mall.application.settlement.service.SettlementRecordService;
import com.lemnt.mall.application.settlement.service.SettlementService;
import com.lemnt.mall.domain.cart.gateway.CartGateway;
import com.lemnt.mall.domain.coupon.gateway.CouponGateway;
import com.lemnt.mall.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.coupon.model.CouponRules;
import com.lemnt.mall.domain.coupon.model.CouponTemplate;
import com.lemnt.mall.domain.member.gateway.MemberLockGateway;
import com.lemnt.mall.domain.member.model.MemberSnapshot;
import com.lemnt.mall.domain.product.gateway.BrandGateway;
import com.lemnt.mall.domain.product.gateway.CategoryGateway;
import com.lemnt.mall.domain.product.gateway.ProductCatalogGateway;
import com.lemnt.mall.domain.product.gateway.ProductCommentGateway;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.Brand;
import com.lemnt.mall.domain.product.model.Category;
import com.lemnt.mall.domain.product.model.Product;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.product.model.ProductSnapshot;
import com.lemnt.mall.domain.product.model.ProductSpu;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.gateway.SettlementGateway;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;
import com.lemnt.mall.domain.settlement.model.Settlement;
import com.lemnt.mall.domain.settlement.model.SettlementRules;
import com.lemnt.mall.infrastructure.settlement.gateway.OrderGatewayImpl;
import com.lemnt.trade.application.aftersale.cmd.AfterSaleCreateCmd;
import com.lemnt.trade.application.aftersale.cmd.AfterSaleRefuseCmd;
import com.lemnt.trade.application.aftersale.cmd.AfterSaleRejectCmd;
import com.lemnt.trade.application.aftersale.cmd.AfterSaleReturnCmd;
import com.lemnt.trade.application.aftersale.service.AfterSaleService;
import com.lemnt.trade.application.fulfillment.cmd.RefundRejectCmd;
import com.lemnt.trade.application.fulfillment.cmd.RefundRequestCmd;
import com.lemnt.trade.application.fulfillment.cmd.ShipmentCreateCmd;
import com.lemnt.trade.application.fulfillment.executor.OrderPageQueryExe;
import com.lemnt.trade.application.aftersale.executor.AfterSalePageQueryExe;
import com.lemnt.trade.application.fulfillment.executor.ShipmentDetailQueryExe;
import com.lemnt.trade.application.fulfillment.executor.ShipmentPageQueryExe;
import com.lemnt.trade.application.fulfillment.qry.OrderPageQry;
import com.lemnt.trade.application.aftersale.qry.AfterSalePageQry;
import com.lemnt.trade.application.fulfillment.qry.ShipmentPageQry;
import com.lemnt.trade.application.fulfillment.service.OrderFulfillmentService;
import com.lemnt.trade.application.order.executor.TradeOrderCloseCmdExe;
import com.lemnt.trade.application.order.service.OrderLifecycleService;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.application.payment.executor.MemberPaymentStatusQueryExe;
import com.lemnt.trade.application.payment.executor.PaymentNotifyCmdExe;
import com.lemnt.trade.application.payment.executor.PaymentPageQueryExe;
import com.lemnt.trade.application.payment.executor.TradePaymentCreateCmdExe;
import com.lemnt.trade.application.payment.qry.MemberPaymentStatusQry;
import com.lemnt.trade.application.payment.qry.PaymentPageQry;
import com.lemnt.trade.application.paymentrefund.executor.PaymentRefundCreateCmdExe;
import com.lemnt.trade.application.paymentrefund.executor.PaymentRefundPageQueryExe;
import com.lemnt.trade.application.paymentrefund.qry.PaymentRefundPageQry;
import com.lemnt.trade.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.fulfillment.gateway.ShipmentGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.payment.model.VerifiedPaymentNotification;
import com.lemnt.trade.domain.paymentrefund.gateway.PaymentRefundChannelGateway;
import com.lemnt.trade.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefundResult;
import com.lemnt.trade.infrastructure.payment.gateway.TradePaymentGatewayImpl;
import com.lemnt.trade.infrastructure.payment.mapper.TradePaymentMapper;
import com.lemnt.trade.infrastructure.paymentrefund.channel.PaymentRefundClient;
import com.lemnt.trade.infrastructure.paymentrefund.gateway.PaymentRefundChannelGatewayImpl;
import com.lemnt.trade.start.TableTestComponents;
import com.lemnt.trade.start.TradeOrderScheduler;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
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
    boolean loseNextCommentResponse;

    TableTestComponents tradeComponents, mallComponents;
    ProductSkuGateway productSkuGateway;
    ProductInventoryService productInventoryService;
    MemberLockGateway checkoutAccountGateway;
    CartGateway cartGateway;
    CouponTemplateGateway couponTemplateGateway;
    CouponGateway couponGateway;
    SettlementRecordService checkoutService;
    InventoryConfirmationGateway inventoryConfirmationGateway;
    ShipmentGateway shipmentGateway;
    AfterSaleGateway refundRequestGateway;
    TradeOrderGateway tradeOrderGateway;
    OrderPersistenceService orderPersistenceService;

    JdbcDataSource tradeSource, mallSource;
    Connection tradeSchema, mallSchema;
    CouponService coupons;
    CartSaveCmdExe cartAdditions;
    CartService cartService;
    CouponIssuanceService couponIssuanceService;
    SettlementGateway settlements;
    MemorySettlementGateway cacheStore;
    com.lemnt.mall.domain.settlement.gateway.SettlementGateway settlementOrderGateway;
    com.lemnt.mall.domain.settlement.gateway.SettlementGateway settlementAbandonGateway;
    Map<String, Settlement> settlementCache =
    new ConcurrentHashMap<>();
    OrderFulfillmentService fulfillmentService;
    RollbackMessages rollbackMessages = new RollbackMessages();
    com.lemnt.trade.application.order.service.OrderRemoteService remotes;

    class RollbackMessages implements com.lemnt.trade.domain.order.gateway.OrderRollbackGateway {
        final java.util.Queue<String> messages = new java.util.concurrent.ConcurrentLinkedQueue<>();
        public void publish(String orderNo) { messages.add(orderNo); }
        int execute(int limit) {
            int completed = 0;
            int size = Math.min(messages.size(), limit);
            var processor = new com.lemnt.trade.application.order.executor.OrderRollbackCmdExe(
                    orderPersistenceService, benefits);
            for (int i = 0; i < size; i++) {
                String no = messages.remove();
                try { processor.execute(no); completed++; }
                catch (RuntimeException exception) { messages.add(no); }
            }
            return completed;
        }
    }
    PaymentRefundClient refundClient;
    TradePaymentGateway payments;
    OrderPlacementService placement;
    OrderLifecycleService lifecycle;
    TradeOrderCloseCmdExe closer;
    TradePaymentCreateCmdExe paymentCreate;
    PaymentNotifyCmdExe notify;
    interface CatalogBridge extends ProductCatalogGateway, com.lemnt.trade.domain.inventory.gateway.InventoryGateway {}
    CatalogBridge bridge;
    com.lemnt.mall.domain.settlement.gateway.OrderGateway orderRpc;
    com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway benefits;
    boolean deliveryPointGift;
    Long sku;
    com.lemnt.member.client.dto.MemberAddressDTO deliveryAddress = new com.lemnt.member.client.dto.MemberAddressDTO(
            7L, 1L, "收件人", "13800000000", 110101L, "原始地址");
    boolean loseNextOrderResponse;
    boolean rejectNextReserve;
    boolean loseNextReserveResponse;
    boolean loseNextConfirmResponse;
    Map<String, SettlementOrder> submittedOrders = new java.util.concurrent.ConcurrentHashMap<>();
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
        {
            bean.setMapperLocations(new org.springframework.core.io.support.PathMatchingResourcePatternResolver()
                    .getResources("classpath*:mapper/*.xml"));
        }
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

    /** 用本地重入锁替代 Redis 传输，保留真实锁模板及事务完成回调。 */
    private com.baomidou.lock.LockTemplate memberLocks() throws Exception {
        var locks = new java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.locks.ReentrantLock>();
        var executor = org.mockito.Mockito.mock(com.baomidou.lock.executor.RedissonLockExecutor.class);
        org.mockito.Mockito.when(executor.renewal()).thenReturn(true);
        org.mockito.Mockito.when(executor.acquire(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong())).thenAnswer(invocation -> {
            var lock = locks.computeIfAbsent(invocation.getArgument(0), key -> new java.util.concurrent.locks.ReentrantLock());
            if (!lock.tryLock(3, java.util.concurrent.TimeUnit.SECONDS)) {
                return null;
            }
            var instance = org.mockito.Mockito.mock(org.redisson.api.RLock.class);
            return instance;
        });
        // LockTemplate 调用执行器解锁；测试替身显式释放对应本地锁。
        org.mockito.Mockito.when(executor.releaseLock(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            var lock = locks.get(invocation.<String>getArgument(0));
            if (lock == null || !lock.isHeldByCurrentThread()) {
                return false;
            }
            lock.unlock();
            return true;
        });
        var properties = new com.baomidou.lock.spring.boot.autoconfigure.Lock4jProperties();
        properties.setPrimaryExecutor(com.baomidou.lock.executor.RedissonLockExecutor.class);
        var template = new com.baomidou.lock.LockTemplate();
        template.setProperties(properties);
        template.setExecutors(java.util.List.of(executor));
        template.afterPropertiesSet();
        return template;
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
        ScriptUtils.executeSqlScript(tradeSchema, new ClassPathResource("trade-sql/V8__order_log.sql"));
        ScriptUtils.executeSqlScript(
                tradeSchema, new ClassPathResource("trade-sql/V2__trade_payment.sql"));
        ScriptUtils.executeSqlScript(
                mallSchema, new ClassPathResource("mall-sql/V10__cart_coupon_checkout.sql"));
        ScriptUtils.executeSqlScript(
                mallSchema, new ClassPathResource("mall-sql/V1__mall_product.sql"));
        ScriptUtils.executeSqlScript(
                mallSchema, new ClassPathResource("mall-sql/V2__mall_catalog.sql"));
        ScriptUtils.executeSqlScript(
                mallSchema, new ClassPathResource("mall-sql/V16__product_management.sql"));
        ScriptUtils.executeSqlScript(
                tradeSchema, new ClassPathResource("trade-sql/V5__order_fulfillment.sql"));
        ScriptUtils.executeSqlScript(
                tradeSchema, new ClassPathResource("trade-sql/V6__payment_refund.sql"));
        ScriptUtils.executeSqlScript(tradeSchema, new ClassPathResource("trade-sql/V9__after_sale_log.sql"));
        ScriptUtils.executeSqlScript(mallSchema, new ClassPathResource("mall-sql/V15__promotion_activity.sql"));
        ScriptUtils.executeSqlScript(tradeSchema, new ClassPathResource("trade-sql/V7__delivery_config.sql"));
        var trade =
        template(tradeSource);
        var products = template(mallSource);
        tradeComponents = new TableTestComponents(trade, tradeSource);
        tradeComponents.get(com.lemnt.trade.domain.delivery.gateway.DeliveryExpressGateway.class).insert(
                new com.lemnt.trade.domain.delivery.model.DeliveryExpress(null, "SF", "顺丰", null, 0, 0));
        mallComponents = new TableTestComponents(products, mallSource);
        mallComponents.register(com.lemnt.mall.domain.member.gateway.MemberLookupGateway.class, id -> new MemberSnapshot(id, 1));
        mallComponents.register(com.lemnt.mall.domain.member.gateway.MemberTradeGateway.class, new com.lemnt.mall.domain.member.gateway.MemberTradeGateway() {
            public com.lemnt.mall.domain.member.model.MemberPointConfig config() {
                return new com.lemnt.mall.domain.member.model.MemberPointConfig(deliveryPointGift, 1, 0, 2);
            }
            public void apply(com.lemnt.mall.domain.member.model.MemberTradeChange change) { }
        });
        var addressGateway = new com.lemnt.mall.infrastructure.member.gateway.MemberDeliveryAddressGatewayImpl();
        org.springframework.test.util.ReflectionTestUtils.setField(addressGateway, "client",
                (com.lemnt.member.client.MemberAddressClient) (owner, id) -> {
                    var address = deliveryAddress;
                    return address != null && address.memberId().equals(owner) && address.id().equals(id) ? address : null;
                });
        mallComponents.register(com.lemnt.mall.domain.member.gateway.MemberDeliveryAddressGateway.class, addressGateway);
        var deliveryGateway = new com.lemnt.mall.infrastructure.settlement.gateway.DeliveryPriceGatewayImpl();
        org.springframework.test.util.ReflectionTestUtils.setField(deliveryGateway, "client",
                new com.lemnt.trade.application.dubbo.TradeDeliveryClientImpl(
                        tradeComponents.get(com.lemnt.trade.application.delivery.service.DeliveryPriceService.class)));
        mallComponents.register(com.lemnt.mall.domain.settlement.gateway.DeliveryPriceGateway.class, deliveryGateway);
        mallComponents.register(com.baomidou.lock.LockTemplate.class, memberLocks());
        refundClient = Mockito.mock(PaymentRefundClient.class);
        Mockito.when(refundClient.channel()).thenReturn("ALIPAY");
        tradeComponents.register(PaymentRefundChannelGateway.class,
                new PaymentRefundChannelGatewayImpl(List.of(refundClient)));
        var commentBridge = new com.lemnt.trade.infrastructure.order.gateway.OrderCommentGatewayImpl();
        tradeComponents.register(com.lemnt.trade.domain.order.gateway.OrderCommentGateway.class, commentBridge);
        var provider = new com.lemnt.trade.application.dubbo.TradeOrderClientImpl(
                tradeComponents.get(com.lemnt.trade.application.order.service.OrderSubmissionService.class),
                tradeComponents.get(OrderPersistenceService.class), null, tradeComponents.get(com.lemnt.trade.application.order.service.OrderCommentService.class));
        var client = new com.lemnt.trade.client.TradeOrderClient() {
            public com.lemnt.trade.client.dto.OrderDTO create(com.lemnt.trade.client.bo.OrderCreateBo value) {
                var actual = new com.lemnt.trade.application.dubbo.TradeOrderClientImpl(
                        tradeComponents.get(com.lemnt.trade.application.order.service.OrderSubmissionService.class),
                        orderPersistenceService, remotes, tradeComponents.get(com.lemnt.trade.application.order.service.OrderCommentService.class));
                var saved = actual.create(value);
                if (loseNextOrderResponse) {
                    loseNextOrderResponse = false;
                    throw new IllegalStateException("交易订单已提交，但 RPC 响应丢失");
                }
                return saved;
            }
            public Long createComment(com.lemnt.trade.client.bo.OrderCommentCreateBo value) {
                return provider.createComment(value);
            }
            public com.lemnt.trade.client.dto.OrderDTO getByOrderNo(String no) {
                return provider.getByOrderNo(no);
            }
        };
        var orderAdapter = new com.lemnt.mall.infrastructure.settlement.gateway.OrderGatewayImpl();
        org.springframework.test.util.ReflectionTestUtils.setField(orderAdapter, "client", client);
        orderRpc = orderAdapter;
        mallComponents.register(com.lemnt.mall.domain.settlement.gateway.OrderGateway.class, orderRpc);
        var commentProvider = new com.lemnt.mall.application.dubbo.ProductCommentClientImpl(mallComponents.get(ProductCommentService.class));
        com.lemnt.mall.client.ProductCommentClient commentClient = new com.lemnt.mall.client.ProductCommentClient() {
            public Long saveSystem(com.lemnt.mall.client.bo.ProductCommentSaveBo value) {
                Long id = commentProvider.saveSystem(value);
                if (loseNextCommentResponse) { loseNextCommentResponse=false; throw new IllegalStateException("系统评价已保存响应丢失"); }
                return id;
            }
            public Long saveVerified(com.lemnt.mall.client.bo.ProductCommentSaveBo value) {
            Long id = commentProvider.saveVerified(value);
            if (loseNextCommentResponse) {
                loseNextCommentResponse = false;
                throw new IllegalStateException("评价已保存，但RPC响应丢失");
            }
            return id;
            }
        };
        org.springframework.test.util.ReflectionTestUtils.setField(commentBridge, "client", commentClient);

        productSkuGateway = mallComponents.get(ProductSkuGateway.class);
        productInventoryService = mallComponents.get(ProductInventoryService.class);
        checkoutAccountGateway = mallComponents.get(MemberLockGateway.class);
        cartGateway = mallComponents.get(CartGateway.class);
        couponTemplateGateway = mallComponents.get(CouponTemplateGateway.class);
        couponGateway = mallComponents.get(CouponGateway.class);
        cartService = mallComponents.get(CartService.class);
        couponIssuanceService = mallComponents.get(CouponIssuanceService.class);
        inventoryConfirmationGateway = tradeComponents.get(InventoryConfirmationGateway.class);
        shipmentGateway = tradeComponents.get(ShipmentGateway.class);
        refundRequestGateway = tradeComponents.get(AfterSaleGateway.class);
        tradeOrderGateway = tradeComponents.get(TradeOrderGateway.class);
        orderPersistenceService = tradeComponents.get(OrderPersistenceService.class);

        cacheStore = new MemorySettlementGateway(settlementCache);
        settlements = cacheStore;
        settlementOrderGateway = settlements;
        settlementAbandonGateway = settlements;
        mallComponents.register(SettlementGateway.class, settlements);
        checkoutService = mallComponents.get(SettlementRecordService.class);

        payments =
        tx(
                new TradePaymentGatewayImpl(trade.getMapper(TradePaymentMapper.class)),
                tradeSource);

        sku =
        productSkuGateway.save(
                Product.create(
                "SKU1", "sku", null, null, new BigDecimal("10.00"), 10))
            .id();
        productSkuGateway.updateStatus(sku, "ON_SHELF");
        bridge =
        new CatalogBridge() {
            public ProductSnapshot findOnShelf(Long id) {
                return mallComponents.get(ProductCatalogGateway.class).findOnShelf(id);
            }

            public Map<Long, ProductSnapshot> findOnShelfBatch(List<Long> ids) {
                return mallComponents.get(ProductCatalogGateway.class).findOnShelfBatch(ids);
            }

            public boolean reserveStock(String no, Long id, Integer quantity) {
                if (rejectNextReserve) { rejectNextReserve = false; return false; }
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
        var member = new MemberValidationService(id -> new MemberSnapshot(id, 1));
        coupons = tx(new CouponService(couponIssuanceService, couponGateway, couponTemplateGateway, member), mallSource);
        cartAdditions = tx(new CartSaveCmdExe(cartGateway, mallComponents.get(MemberLockGateway.class), cartService, member, bridge), mallSource);
        placement =
        new OrderPlacementService(tx(new com.lemnt.mall.application.settlement.service.SettlementPreparationService(mallComponents.get(com.lemnt.mall.application.promotion.service.ActivityPricingService.class), mallComponents.get(com.lemnt.mall.application.settlement.service.SettlementDeliveryService.class), mallComponents.get(CartGateway.class), mallComponents.get(MemberLockGateway.class), mallComponents.get(SettlementRecordService.class), mallComponents.get(CouponGateway.class), mallComponents.get(CouponTemplateGateway.class), orderRpc, settlements, bridge, member), mallSource), orderRpc);

        fulfillmentService =
        tx(new OrderFulfillmentService(tradeComponents.get(InventoryConfirmationGateway.class), tradeComponents.get(OrderPersistenceService.class), tradeComponents.get(AfterSaleGateway.class), tradeComponents.get(ShipmentGateway.class), tradeComponents.get(TradeOrderGateway.class), tradeComponents.get(com.lemnt.trade.domain.delivery.gateway.DeliveryExpressGateway.class), tradeComponents.get(com.lemnt.trade.application.order.service.OrderLogService.class)), tradeSource);
        lifecycle =
        tx(
                new OrderLifecycleService(tradeComponents.get(InventoryConfirmationGateway.class), tradeComponents.get(OrderPersistenceService.class), tradeComponents.get(TradeOrderGateway.class), tradeComponents.get(com.lemnt.trade.application.order.service.OrderLogService.class)),
                tradeSource);
        var handler = tx(new com.lemnt.mall.application.dubbo.SettlementLifecycleClientImpl(
                mallComponents.get(com.lemnt.mall.domain.member.gateway.MemberTradeGateway.class), checkoutService, orderRpc, productInventoryService), mallSource);
        benefits = new com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway() {
            public void verify(String no) { handler.verify(no); }
            public void reserved(String no) { handler.reserved(no); }
            public void paid(String no) { handler.paid(no); }
            public void closed(String no) { handler.closed(no); }
            public void refunded(String no, String refundNo, java.util.Map<Long, Integer> quantities, java.math.BigDecimal amount) {
                handler.refunded(no, refundNo, quantities.entrySet().stream().map(item ->
                        new com.lemnt.mall.client.bo.RefundStockBo(item.getKey(), item.getValue())).toList(), amount);
            }
        };
        tradeComponents.register(com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway.class, benefits);
        remotes = new com.lemnt.trade.application.order.service.OrderRemoteService(
                orderPersistenceService, bridge, benefits, lifecycle, rollbackMessages);
        closer =
                new TradeOrderCloseCmdExe(tx(new com.lemnt.trade.application.order.service.OrderCloseService(orderPersistenceService, tradeOrderGateway, payments, p -> false, tradeComponents.get(com.lemnt.trade.application.order.service.OrderLogService.class)), tradeSource), remotes);
        paymentCreate = tx(new TradePaymentCreateCmdExe(tradeComponents.get(OrderPersistenceService.class), payments), tradeSource);
        notify = new PaymentNotifyCmdExe(tx(new com.lemnt.trade.application.payment.service.PaymentNotificationService(orderPersistenceService, tradeOrderGateway, payments, tradeComponents.get(com.lemnt.trade.application.order.service.OrderLogService.class)), tradeSource), remotes);
        cartService.saveCart(1L, sku, 2, true);
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

    RollbackMessages worker() { return rollbackMessages; }

    CouponTemplate campaign(int quota, BigDecimal amount) {
        var now = couponTemplateGateway.now();
        return couponTemplateGateway.createTemplate(
                CouponRules.template(
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

    SettlementOrder place(String key, Long coupon) {
        String token =
        quoteTokens.computeIfAbsent(
                key,
                k ->
                SettlementRules.quoteToken(
                placement.quote(
                new SettlementQuoteQry(1L, List.of(sku), coupon))));
        return placement.checkoutCart(new SettlementOrderSubmitCmd(1L, key, List.of(sku), coupon, token));
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
    void successfulCheckoutUsesServerPricesLocksCouponAndConsumesCartAfterReservation() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, new BigDecimal("3.00")).id());
        var order = place("SYNC", coupon.id());
        assertEquals("PENDING_PAYMENT", order.status());
        assertEquals(new BigDecimal("17.00"), order.totalAmount());
        assertEquals(8, productSkuGateway.findById(sku).stock());
        assertEquals("LOCKED", couponGateway.coupon(1L, coupon.id(), false).status());
        assertTrue(cartGateway.cart(1L).isEmpty());
        assertTrue(rollbackMessages.messages.isEmpty());
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        var notice = new VerifiedPaymentNotification(payment.paymentNo(), order.orderNo(),
                "ALIPAY", "SYNC-PAY", order.totalAmount(), "NOTICE-SYNC");
        notify.execute(notice);
        notify.execute(notice);
        assertEquals("USED", couponGateway.coupon(1L, coupon.id(), false).status());
        assertTrue(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
        assertEquals(8, productSkuGateway.findById(sku).stock());
    }

    /** 库存和券在商城同一事务补偿，核券回滚失败不能先归还库存。 */
    @Test
    void stockAndCouponRollbackCommitTogetherAndMqRetryIsIdempotent() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, BigDecimal.ONE).id());
        var order = place("ROLLBACK-ATOMIC", coupon.id());
        assertTrue(closer.execute(order.orderNo()));
        sql(mallSource, "ALTER TABLE mall_coupon ADD CONSTRAINT block_release CHECK(status<>'AVAILABLE')");
        assertEquals(0, worker().execute(50));
        assertEquals(8, productSkuGateway.findById(sku).stock());
        assertEquals("LOCKED", couponGateway.coupon(1L, coupon.id(), false).status());
        sql(mallSource, "ALTER TABLE mall_coupon DROP CONSTRAINT block_release");
        assertEquals(1, worker().execute(50));
        assertEquals(10, productSkuGateway.findById(sku).stock());
        assertEquals("AVAILABLE", couponGateway.coupon(1L, coupon.id(), false).status());
        rollbackMessages.publish(order.orderNo());
        assertEquals(1, worker().execute(50));
        assertEquals(10, productSkuGateway.findById(sku).stock());
    }

    /** 同步核券失败不继续扣库存，修复后原请求可以重试。 */
    @Test
    void synchronousCouponCheckMustSucceedBeforeInventoryReservation() throws Exception {
        var original = remotes;
        var rejected = Mockito.mock(com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway.class,
                AdditionalAnswers.delegatesTo(benefits));
        Mockito.doThrow(new IllegalStateException("同步核券失败")).when(rejected).verify(Mockito.anyString());
        tradeComponents.register(com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway.class, benefits);
        remotes = new com.lemnt.trade.application.order.service.OrderRemoteService(
                orderPersistenceService, bridge, rejected, lifecycle, rollbackMessages);
        assertThrows(IllegalStateException.class, () -> place("VERIFY-FAIL", null));
        assertEquals(10, productSkuGateway.findById(sku).stock());
        assertEquals(1, cartGateway.cart(1L).size());
        remotes = original;
        var order = place("VERIFY-FAIL", null);
        assertEquals("PENDING_PAYMENT", order.status());
        assertEquals(8, productSkuGateway.findById(sku).stock());
    }

    @Test
    void idempotentRetryReturnsOriginalPriceEvenAfterCartDeletionAndCatalogPriceChange()
    throws Exception {
        var order = place("REQ1", null);
        worker().execute(50);
        sql(mallSource, "UPDATE mall_product_sku SET price=99.00 WHERE id=" + sku);
        assertEquals(order.orderNo(), place("REQ1", null).orderNo());
        assertEquals(new BigDecimal("20.00"), checkoutService.receipt(order.orderNo()).payableAmount());
        assertEquals(1, count("trade_order"));
        assertThrows(IllegalStateException.class, () -> place("REQ1", 99L));
    }

    @Test
    void lostRpcResponseIsRetriedAfterRestartWithoutDoubleDebit() throws Exception {
        loseNextReserveResponse = true;
        assertThrows(IllegalStateException.class, () -> place("LOST", null));
        var record = checkoutService.findRequest(1L, "LOST");
        assertEquals(8, productSkuGateway.findById(sku).stock());
        assertEquals("RESERVING", orderPersistenceService.findByOrderNo(record.orderNo()).status());
        var order = place("LOST", null);
        assertEquals(record.orderNo(), order.orderNo());
        assertEquals("PENDING_PAYMENT", order.status());
        assertEquals(8, productSkuGateway.findById(sku).stock());
    }

    @Test
    void closingAfterAmbiguousReserveReleasesCouponAndFencesLateRequests() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, BigDecimal.ONE).id());
        loseNextReserveResponse = true;
        assertThrows(IllegalStateException.class, () -> place("LOST-CLOSE", coupon.id()));
        var record = checkoutService.findRequest(1L, "LOST-CLOSE");
        assertTrue(closer.execute(record.orderNo()));
        assertEquals(8, productSkuGateway.findById(sku).stock());
        assertEquals("LOCKED", couponGateway.coupon(1L, coupon.id(), false).status());
        assertEquals(1, worker().execute(50));
        assertEquals(10, productSkuGateway.findById(sku).stock());
        assertEquals("AVAILABLE", couponGateway.coupon(1L, coupon.id(), false).status());
        assertFalse(productInventoryService.reserveStock(record.orderNo(), sku, 2));
        assertEquals(1, cartGateway.cart(1L).size());
    }

    @Test
    void stockRejectionDurablyClosesAndCancelsUnattemptedProducts() throws Exception {
        rejectNextReserve = true;
        assertThrows(IllegalStateException.class, () -> place("REJECT", null));
        var order = orderRpc.findByOrderNo(checkoutService.findRequest(1L, "REJECT").orderNo());
        assertEquals("CLOSED", order.status());
        assertEquals(1, worker().execute(50));
        assertFalse(productInventoryService.reserveStock(order.orderNo(), sku, 2));
        assertEquals(10, productSkuGateway.findById(sku).stock());
    }

    @Test
    void adminIssuanceDoesNotConsumePublicQuotaAndEnforcesPerMemberLimit() throws Exception {
        var template = campaign(2, BigDecimal.ONE);
        var pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (long i = 1; i <= 8; i++) {
                final long member = i;
                tasks.add(
                        () -> {
                    try {
                        couponIssuanceService.issue(member, template.id());
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
            assertEquals(8, successes);
            assertEquals(8, couponTemplateGateway.template(template.id()).issuedCount());
            for (long i = 1; i <= 8; i++) {
                if (!couponGateway.coupons(i).isEmpty()) {
                    final long member = i;
                    assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                            () -> couponIssuanceService.issue(member, template.id()));
                }
            }
            assertEquals(8, couponTemplateGateway.template(template.id()).issuedCount());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void ownershipAndSelectionAreCheckedWhileDisabledTemplatePreservesIssuedCoupon()
    throws Exception {
        var template = campaign(10, BigDecimal.ONE);
        var foreign = couponIssuanceService.issue(2L, template.id());
        assertThrows(IllegalArgumentException.class, () -> place("FOREIGN", foreign.id()));
        var own = couponIssuanceService.issue(1L, template.id());
        couponTemplateGateway.enableTemplate(template.id(), false);
        assertEquals("PENDING_PAYMENT", place("DISABLED", own.id()).status());
        cartService.saveCart(1L, sku, 2, false);
        assertThrows(IllegalArgumentException.class, () -> place("NOT-SELECTED", null));
        assertEquals(1, count("trade_order"));

    }

    @Test
    void cartMutationOrDeleteReaddDuringReservationIsNotRemovedByFinalizer() throws Exception {
        loseNextReserveResponse = true;
        assertThrows(IllegalStateException.class, () -> place("V1", null));
        cartService.saveCart(1L, sku, 3, true);
        place("V1", null);
        assertEquals(3, cartGateway.cart(1L).get(0).quantity());
        loseNextReserveResponse = true;
        assertThrows(IllegalStateException.class, () -> place("V2", null));
        var oldId = cartGateway.cart(1L).get(0).id();
        cartService.deleteCart(1L, sku);
        cartService.saveCart(1L, sku, 1, true);
        var order = place("V2", null);
        assertNotEquals(oldId, cartGateway.cart(1L).get(0).id());
        assertEquals(1, cartGateway.cart(1L).get(0).quantity());
        assertEquals("PENDING_PAYMENT", order.status());
    }

    @Test
    void zeroPayableQuoteIsExplicitlyUnsupportedAtPlacementAndRollsBackLocks() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, new BigDecimal("50.00")).id());
        assertEquals(
                new BigDecimal("0.00"),
                placement
                .quote(new SettlementQuoteQry(1L, List.of(sku), coupon.id()))
                .payableAmount());
        assertThrows(IllegalArgumentException.class, () -> place("ZERO", coupon.id()));
        assertEquals("AVAILABLE", couponGateway.coupon(1L, coupon.id(), false).status());
        assertEquals(0, count("trade_order"));
    }

    @Test
    void couponInvariantFailureRetainsPaidOrderAndRetriesMallCallback() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, BigDecimal.ONE).id());
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
        assertThrows(IllegalStateException.class, () -> notify.execute(notice));
        assertEquals("SUCCESS", payments.findByPaymentNo(payment.paymentNo()).status());
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void reservationTimeoutIsNotStarvedByBlockedPendingPayments() throws Exception {
        loseNextReserveResponse = true;
        assertThrows(IllegalStateException.class, () -> place("EXPIRE", null));
        var order = orderRpc.findByOrderNo(checkoutService.findRequest(1L, "EXPIRE").orderNo());
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
        new TradeOrderScheduler(closer, tradeOrderGateway).closeExpired();
        assertEquals("CLOSED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals("PENDING_PAYMENT", orderPersistenceService.findByOrderNo("BLOCK0").status());
    }

    @Test
    void changedPriceAfterPreviewRequiresExplicitReconfirmation() throws Exception {
        String token =
        SettlementRules.quoteToken(
                placement.quote(new SettlementQuoteQry(1L, List.of(sku), null)));
        sql(mallSource, "UPDATE mall_product_sku SET price=11.00 WHERE id=" + sku);
        assertThrows(
                IllegalStateException.class,
                () ->
                placement.checkoutCart(
                new SettlementOrderSubmitCmd(1L, "STALE", List.of(sku), null, token)));
        assertEquals(0, count("trade_order"));
        assertEquals(10, productSkuGateway.findById(sku).stock());
    }

    @Test
    void unavailableCartItemCanBeDeselectedButNotNewlySelected() {
        productSkuGateway.updateStatus(sku, "OFF_SHELF");
        var saver =
        tx(
                new CartSaveCmdExe(mallComponents.get(CartGateway.class), mallComponents.get(MemberLockGateway.class), mallComponents.get(CartService.class), new MemberValidationService(id -> new MemberSnapshot(id, 1)), bridge),
                mallSource);
        assertFalse(saver.execute(new CartSaveCmd(1L, sku, 2, false)).selected());
        assertThrows(
                IllegalArgumentException.class,
                () -> saver.execute(new CartSaveCmd(1L, sku, 2, true)));
    }

    @Test
    void expiredCouponCannotBecomeLockedOrCreateOrder() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, BigDecimal.ONE).id());
        sql(mallSource,
                "UPDATE mall_coupon SET valid_from=TIMESTAMP '2000-01-01"
                + " 00:00:00',valid_until=TIMESTAMP '2001-01-01 00:00:00' WHERE id="
                + coupon.id());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> place("EXPIRED", coupon.id()));
        assertEquals(0, count("trade_order"));
        assertEquals("AVAILABLE", couponGateway.coupon(1L, coupon.id(), false).status());
    }

    @Test
    void concurrentIdenticalCheckoutKeyCreatesOnlyOneDurableIntent() throws Exception {
        var token =
        SettlementRules.quoteToken(
                placement.quote(new SettlementQuoteQry(1L, List.of(sku), null)));
        var command = new SettlementOrderSubmitCmd(1L, "SAME", List.of(sku), null, token);
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

            assertEquals(8, productSkuGateway.findById(sku).stock());
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
        var coupon = couponIssuanceService.issue(1L, campaign(10, BigDecimal.ONE).id());
        var pool = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Boolean>> tasks =
            List.of(
                    () -> {
                try {
                    placement.createDirect(
                            new SettlementDirectCmd(
                            1L,
                            List.of(
                            new com.lemnt.mall.application.settlement.cmd.SettlementDirectCmd.OrderLine(
                            sku, 2)),
                            "A",
                            coupon.id()));
                    return true;
                } catch (IllegalArgumentException | com.lemnt.common.core.exception.ServiceException ex) {
                    return false;
                }
            },
                    () -> {
                try {
                    placement.createDirect(
                            new SettlementDirectCmd(
                            1L,
                            List.of(
                            new com.lemnt.mall.application.settlement.cmd.SettlementDirectCmd.OrderLine(
                            sku, 2)),
                            "B",
                            coupon.id()));
                    return true;
                } catch (IllegalArgumentException | com.lemnt.common.core.exception.ServiceException ex) {
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

    SettlementOrder paidOrder(Long coupon) {
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

    private Long configureDeliveryProduct() {
        var management = tradeComponents.get(com.lemnt.trade.application.delivery.service.DeliveryManagementService.class);
        Long template = management.createTemplate(new com.lemnt.trade.application.delivery.cmd.DeliveryExpressTemplateCreateCmd(
                "按件运费", 1, 0, List.of(new com.lemnt.trade.application.delivery.cmd.DeliveryChargeCmd(
                        null, List.of(110101), BigDecimal.ONE, 500, BigDecimal.ONE, 150)), List.of()));
        var categories = mallComponents.get(com.lemnt.mall.domain.product.gateway.CategoryGateway.class);
        var brands = mallComponents.get(com.lemnt.mall.domain.product.gateway.BrandGateway.class);
        var category = categories.saveCategory(new com.lemnt.mall.domain.product.model.Category(
                null, null, "快递分类", null, 0, "ENABLED"));
        var brand = brands.saveBrand(new com.lemnt.mall.domain.product.model.Brand(
                null, "快递品牌", null, null, 0, "ENABLED"));
        var spu = mallComponents.get(com.lemnt.mall.domain.product.gateway.ProductSpuGateway.class).insertSpu(
                new com.lemnt.mall.domain.product.model.ProductSpu(null, "DELIVERY-SPU", "快递商品", category.id(),
                        brand.id(), null, null, "ON_SHELF", List.of(), List.of(1), template, null, null, null, null));
        mallComponents.get(com.lemnt.mall.infrastructure.product.mapper.ProductSkuMapper.class).update(null,
                com.baomidou.mybatisplus.core.toolkit.Wrappers
                        .<com.lemnt.mall.infrastructure.product.dataobject.ProductSkuDO>lambdaUpdate()
                        .eq(com.lemnt.mall.infrastructure.product.dataobject.ProductSkuDO::getId, sku)
                        .set(com.lemnt.mall.infrastructure.product.dataobject.ProductSkuDO::getSpuId, spu.id())
                        .set(com.lemnt.mall.infrastructure.product.dataobject.ProductSkuDO::getWeight, new BigDecimal("1.25"))
                        .set(com.lemnt.mall.infrastructure.product.dataobject.ProductSkuDO::getVolume, new BigDecimal("0.1")));
        return template;
    }

    @Test
    void deliveryQuotePersistsRecipientAndFreightAndPartialRefundUsesFrozenPaidLine() throws Exception {
        deliveryPointGift = true;
        configureDeliveryProduct();
        skuProperties(List.of(new com.lemnt.mall.domain.product.model.ProductSkuSpec(null, "尺码", null, "L")));
        cartService.saveCart(1L, sku, 2, true);
        var service = new com.lemnt.mall.application.settlement.service.SettlementService(
                checkoutService, orderRpc, placement, settlements);
        var cached = service.create(new SettlementQuoteQry(1L, List.of(sku), null, false, 7L));
        assertEquals(new BigDecimal("6.50"), cached.quote().delivery().deliveryPrice());
        assertEquals(new BigDecimal("26.50"), cached.quote().payableAmount());
        assertEquals(53L, cached.quote().givePoint());
        var receipt = service.submit(1L, cached.settlementId(), "DELIVERY-ORDER");
        var saved = orderPersistenceService.findByOrderNo(receipt.orderNo());
        assertEquals(new BigDecimal("26.50"), saved.totalAmount());
        assertEquals("原始地址", saved.delivery().receiverDetailAddress());
        assertEquals(new BigDecimal("6.50"), saved.items().get(0).deliveryPrice());
        assertEquals(saved.totalAmount(), saved.items().get(0).payAmount());
        assertEquals("快递商品", saved.items().get(0).spuName());
        assertEquals("尺码", saved.items().get(0).properties().get(0).propertyName());
        assertEquals("L", saved.items().get(0).properties().get(0).valueName());
        deliveryAddress = new com.lemnt.member.client.dto.MemberAddressDTO(
                7L, 1L, "新收件人", "13800000000", 110101L, "修改后的地址");
        assertEquals(receipt, service.submit(1L, cached.settlementId(), "DELIVERY-ORDER"));
        var payment = paymentCreate.execute(receipt.orderNo(), "ALIPAY");
        notify.execute(new VerifiedPaymentNotification(payment.paymentNo(), receipt.orderNo(), "ALIPAY",
                "DELIVERY-PAY", saved.totalAmount(), "DELIVERY-NOTICE"));
        var shipped = fulfillmentService.ship(new ShipmentCreateCmd(receipt.orderNo(), "SHIP-FROZEN", "SF",
                "TRACK-FROZEN", null, null, null));
        assertEquals("原始地址", shipped.address());
        assertEquals("收件人", shipped.recipientName());
        var afterSales = tradeComponents.get(com.lemnt.trade.application.aftersale.service.AfterSaleService.class);
        var line = afterSales.items(receipt.orderNo()).get(0);
        assertEquals(new BigDecimal("26.50"), line.refundableAmount());
        var sale = afterSales.create(new com.lemnt.trade.application.aftersale.cmd.AfterSaleCreateCmd(
                receipt.orderNo(), "DELIVERY-REFUND", line.orderItemId(), 1, "REFUND_ONLY", "部分退货"));
        assertEquals(new BigDecimal("13.25"), sale.amount());
        afterSales.agree(sale.refundNo());
        Mockito.when(refundClient.refund(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenAnswer(call -> refundSuccess(call.getArgument(0)));
        var result = tradeComponents.get(PaymentRefundCreateCmdExe.class).execute(sale.refundNo());
        assertEquals("SUCCESS", result.status());
        assertEquals(new BigDecimal("13.25"), result.amount());
        assertEquals("原始地址", orderPersistenceService.findByOrderNo(receipt.orderNo())
                .delivery().receiverDetailAddress());
    }

    @Test
    void foreignAddressAndChangedAddressOrFreightCannotBeSubmittedFromOldQuote() throws Exception {
        Long templateId = configureDeliveryProduct();
        cartService.saveCart(1L, sku, 2, true);
        var service = new com.lemnt.mall.application.settlement.service.SettlementService(
                checkoutService, orderRpc, placement, settlements);
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> service.create(new SettlementQuoteQry(1L, List.of(sku), null, false, 8L)));
        var original = service.create(new SettlementQuoteQry(1L, List.of(sku), null, false, 7L));
        deliveryAddress = new com.lemnt.member.client.dto.MemberAddressDTO(
                7L, 1L, "收件人", "13800000000", 110101L, "预览后改地址");
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> service.submit(1L, original.settlementId(), "CHANGED-ADDRESS"));
        assertEquals(0, count("trade_order"));
        assertEquals(10, productSkuGateway.findById(sku).stock());
        var fresh = service.create(new SettlementQuoteQry(1L, List.of(sku), null, false, 7L));
        tradeComponents.get(com.lemnt.trade.infrastructure.delivery.mapper.DeliveryExpressTemplateChargeMapper.class)
                .update(null, com.baomidou.mybatisplus.core.toolkit.Wrappers
                        .<com.lemnt.trade.infrastructure.delivery.dataobject.DeliveryExpressTemplateChargeDO>lambdaUpdate()
                        .eq(com.lemnt.trade.infrastructure.delivery.dataobject.DeliveryExpressTemplateChargeDO::getTemplateId,
                                templateId)
                        .set(com.lemnt.trade.infrastructure.delivery.dataobject.DeliveryExpressTemplateChargeDO::getStartPrice,
                                700));
        var conflict = assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> service.submit(1L, fresh.settlementId(), "CHANGED-FREIGHT"));
        assertEquals(409, conflict.getCode());
        assertEquals(0, count("trade_order"));
    }

    @Test
    void disabledCompanyBlocksNewShipmentButNeverBlocksExistingShipmentReplay() throws Exception {
        var order = paidOrder(null);
        var companies = tradeComponents.get(com.lemnt.trade.domain.delivery.gateway.DeliveryExpressGateway.class);
        var company = companies.byCode("SF");
        companies.update(new com.lemnt.trade.domain.delivery.model.DeliveryExpress(
                company.id(), company.code(), company.name(), company.logo(), company.sort(), 1));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> fulfillmentService.ship(shipment(order.orderNo())));
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertNull(shipmentGateway.shipment(order.orderNo()));
        companies.update(new com.lemnt.trade.domain.delivery.model.DeliveryExpress(
                company.id(), company.code(), company.name(), company.logo(), company.sort(), 0));
        var shipped = fulfillmentService.ship(shipment(order.orderNo()));
        companies.delete(company.id());
        assertEquals(shipped, fulfillmentService.ship(shipment(order.orderNo())));
    }

    @Test
    void shippingRequiresAllStockConfirmedAndReceiptReplaysKeepOriginalTimes() throws Exception {
        var order = paidOrder(null);
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
        var postShipmentSale = requestAfterSale(new RefundRequestCmd(order.orderNo(), "R1", "申请"));
        assertEquals("REQUESTED", postShipmentSale.status());
        rejectAfterSale(new RefundRejectCmd(postShipmentSale.refundNo(), "本用例继续验证收货"));
        var received = fulfillmentService.receive(order.orderNo());
        assertNotNull(received.receivedAt());
        assertEquals(received.receivedAt(), tradeComponents.get(com.lemnt.trade.infrastructure.order.mapper.TradeOrderMapper.class)
                .selectById(order.id()).getReceiveTime());
        assertEquals(received, fulfillmentService.receive(order.orderNo()));
        assertEquals(received, fulfillmentService.ship(shipment(order.orderNo())));
        assertEquals("COMPLETED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals(8, productSkuGateway.findById(sku).stock());
    }

    @Test
    void refundRequestCopiesDiscountedPaymentAndBlocksShippingUntilRejected() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, new BigDecimal("3.00")).id());
        var order = paidOrder(coupon.id());
        worker().execute(50);
        var cmd = new RefundRequestCmd(order.orderNo(), "R1", "不再需要");
        var request = requestAfterSale(cmd);
        assertEquals(new BigDecimal("17.00"), request.amount());
        assertEquals("REQUESTED", request.status());
        assertEquals(request, requestAfterSale(cmd));
        assertTrue(refundRequestGateway.activeRefund(order.orderNo()));
        assertThrows(
                IllegalStateException.class,
                () ->
                requestAfterSale(
                new RefundRequestCmd(order.orderNo(), "R1", "不同原因")));
        assertThrows(
                IllegalStateException.class,
                () ->
                requestAfterSale(
                new RefundRequestCmd(order.orderNo(), "R2", "第二申请")));
        assertThrows(
                IllegalStateException.class,
                () -> fulfillmentService.ship(shipment(order.orderNo())));
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals("SUCCESS", payments.findByPaymentNo(request.paymentNo()).status());
        assertEquals("USED", couponGateway.coupon(1L, coupon.id(), false).status());
        assertEquals(8, productSkuGateway.findById(sku).stock());
        var rejected =
        rejectAfterSale(new RefundRejectCmd(request.refundNo(), "核验后驳回"));
        assertEquals("REJECTED", rejected.status());
        assertEquals(
                rejected,
                rejectAfterSale(new RefundRejectCmd(request.refundNo(), "核验后驳回")));
        assertEquals(rejected, requestAfterSale(cmd));
        assertFalse(refundRequestGateway.activeRefund(order.orderNo()));
        assertThrows(
                IllegalStateException.class,
                () ->
                rejectAfterSale(
                new RefundRejectCmd(request.refundNo(), "改写审核")));
        fulfillmentService.ship(shipment(order.orderNo()));
    }

    @Test
    void rejectedRequestAllowsNewKeyButNeverReopensOriginalRequest() throws Exception {
        var order = paidOrder(null);
        var first =
        requestAfterSale(new RefundRequestCmd(order.orderNo(), "R1", "原因"));
        rejectAfterSale(new RefundRejectCmd(first.refundNo(), "驳回"));
        var second =
        requestAfterSale(
                new RefundRequestCmd(order.orderNo(), "R2", "补充材料"));
        assertNotEquals(first.refundNo(), second.refundNo());
        assertEquals("REQUESTED", second.status());
        assertEquals(2, count("trade_after_sale"));
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
                requestAfterSale(
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
                requestAfterSale(
                new RefundRequestCmd(order.orderNo(), "R1", "原因")));
        assertEquals(0, count("trade_after_sale"));
    }

    @Test
    void concurrentShipmentAndAfterSaleSerializeAndPreserveValidFulfillmentState() throws Exception {
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
                    requestAfterSale(
                            new RefundRequestCmd(order.orderNo(), "R1", "原因"));
                    return true;
                } catch (IllegalStateException ex) {
                    return false;
                }
            };
            var a = pool.submit(ship);
            var b = pool.submit(refund);
            start.countDown();
            boolean shipped = a.get(15, TimeUnit.SECONDS);
            assertTrue(b.get(15, TimeUnit.SECONDS));
            assertEquals(shipped, shipmentGateway.shipment(order.orderNo()) != null);
            assertTrue(refundRequestGateway.activeRefund(order.orderNo()));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void readQueriesExposeAccuratePagingAndValidation() {
        var order = paidOrder(null);
        worker().execute(50);
        var request =
        requestAfterSale(new RefundRequestCmd(order.orderNo(), "R1", "原因"));
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
        new AfterSalePageQueryExe(tradeComponents.get(AfterSaleGateway.class));
        assertEquals(
                request.refundNo(),
                refunds.execute(
                new AfterSalePageQry(
                order.orderNo(), "REQUESTED", 1, 20))
                .getRecords()
                .get(0)
                .refundNo());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                pages.execute(
                new OrderPageQry(
                null, "UNKNOWN", 1, 20)));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                refunds.execute(
                new AfterSalePageQry(
                null, null, 0, 20)));
        assertThrows(
                IllegalArgumentException.class, () -> fulfillmentService.receive(order.orderNo()));
    }

    @Test
    void ambiguousStockConfirmCannotUnlockShipmentUntilRetryReturnsSuccess() throws Exception {
        var order = place("CONFIRM-LOST", null);
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        var notice = new VerifiedPaymentNotification(payment.paymentNo(), order.orderNo(),
                "ALIPAY", "CH-LOST", order.totalAmount(), "NOTICE-LOST");
        loseNextConfirmResponse = true;
        assertThrows(IllegalStateException.class, () -> notify.execute(notice));
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertFalse(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
        assertThrows(IllegalStateException.class, () -> fulfillmentService.ship(shipment(order.orderNo())));
        notify.execute(notice);
        assertTrue(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
        fulfillmentService.ship(shipment(order.orderNo()));
        assertEquals(8, productSkuGateway.findById(sku).stock());
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
        var failing = tx(new OrderFulfillmentService(tradeComponents.get(InventoryConfirmationGateway.class), tradeComponents.get(OrderPersistenceService.class), tradeComponents.get(AfterSaleGateway.class), tradeComponents.get(ShipmentGateway.class), failure, tradeComponents.get(com.lemnt.trade.domain.delivery.gateway.DeliveryExpressGateway.class), tradeComponents.get(com.lemnt.trade.application.order.service.OrderLogService.class)), tradeSource);
        assertThrows(IllegalStateException.class, () -> failing.ship(shipment(order.orderNo())));
        assertNull(shipmentGateway.shipment(order.orderNo()));
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        fulfillmentService.ship(shipment(order.orderNo()));
        Mockito.doReturn(false)
            .when(failure)
            .completeReceipt(Mockito.eq(order.orderNo()), Mockito.any());
        assertThrows(IllegalStateException.class, () -> failing.receive(order.orderNo()));
        assertNull(shipmentGateway.shipment(order.orderNo()).receivedAt());
        assertNull(tradeComponents.get(com.lemnt.trade.infrastructure.order.mapper.TradeOrderMapper.class)
                .selectById(order.id()).getReceiveTime());
        assertEquals("SHIPPED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void schemaDoesNotInferStockConfirmationFromPaidStatus() throws Exception {
        var order = paidOrder(null);
        assertTrue(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
        sql(tradeSource, "DROP TABLE trade_payment_refund");
        sql(tradeSource, "DROP TABLE trade_after_sale_log");
        sql(tradeSource, "DROP TABLE trade_after_sale");
        sql(tradeSource, "DROP TABLE trade_shipment");
        sql(tradeSource, "DROP TABLE trade_inventory_confirmation");
        ScriptUtils.executeSqlScript(tradeSchema, new ClassPathResource("trade-sql/V5__order_fulfillment.sql"));
        ScriptUtils.executeSqlScript(tradeSchema, new ClassPathResource("trade-sql/V6__payment_refund.sql"));
        ScriptUtils.executeSqlScript(tradeSchema, new ClassPathResource("trade-sql/V9__after_sale_log.sql"));
        assertFalse(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
        remotes.paid(order.orderNo());
        assertTrue(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
    }

    @Test
    void consumerReceiptChecksOwnerAgainstPersistedOrderAndCheckout() {
        var order = place("APP-OWNER", null);
        var query =
        new SettlementReceiptQueryExe(mallComponents.get(SettlementRecordService.class), orderRpc);
        assertEquals(order.orderNo(), query.execute(1L, order.orderNo()).orderNo());
        assertNull(query.execute(2L, order.orderNo()));
        assertNull(query.execute(1L, "MISSING"));
    }

    Settlement settlement() {
        var value = placement.prepareSettlement(new SettlementQuoteQry(1L, List.of(sku), null));
        settlements.cache(value);
        return value;
    }

    /** 报价仅缓存；订单已提交但响应丢失时，同一请求只能产生一张订单。 */
    @Test
    void redisSettlementSubmissionRetriesAfterLostOrderResponse() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, BigDecimal.ONE).id());
        var value = placement.prepareSettlement(new SettlementQuoteQry(1L, List.of(sku), coupon.id()));
        settlements.cache(value);
        assertEquals(0, count("trade_order"));
        assertEquals(0, cacheStore.submissions.size());
        loseNextOrderResponse = true;
        assertThrows(IllegalStateException.class,
                () -> placement.submitSettlement(1L, value.id(), "LOST-ORDER-ACK", settlements));
        assertEquals(1, count("trade_order"));

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
        assertEquals(1, cacheStore.bindings.size());
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
        cartService.saveCart(1L, sku, 3, true);
        var order = placement.submitSettlement(1L, value.id(), "S1", settlements);
        assertEquals(2, orderPersistenceService.findByOrderNo(order.orderNo()).items().get(0).quantity());
        worker().execute(50);
        assertEquals(3, cartGateway.cart(1L).get(0).quantity());
    }

    @Test
    void settlementRejectsChangedPriceWithoutMutatingSnapshotOrOrder() throws Exception {
        var value = settlement();
        sql(mallSource, "UPDATE mall_product_sku SET price=99.00 WHERE id=" + sku);
        assertThrows(
                com.lemnt.common.core.exception.ServiceException.class,
                () -> placement.submitSettlement(1L, value.id(), "S1", settlements));
        assertEquals(
                new BigDecimal("20.00"), settlementCache.get(value.id()).quote().payableAmount());
        assertEquals(0, count("trade_order"));
        assertEquals(0, cacheStore.bindings.size());
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
    void settlementBindingFailurePreventsRemoteOrderCreation() throws Exception {
        var value = settlement();
        var failing = Mockito.mock(com.lemnt.mall.domain.settlement.gateway.SettlementGateway.class,
                AdditionalAnswers.delegatesTo(settlementOrderGateway));
        Mockito.doThrow(new IllegalStateException("bind failed"))
            .when(failing)
            .bind(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString());
        var failedPlacement = new OrderPlacementService(tx(new com.lemnt.mall.application.settlement.service.SettlementPreparationService(mallComponents.get(com.lemnt.mall.application.promotion.service.ActivityPricingService.class), mallComponents.get(com.lemnt.mall.application.settlement.service.SettlementDeliveryService.class), mallComponents.get(CartGateway.class), mallComponents.get(MemberLockGateway.class), mallComponents.get(SettlementRecordService.class), mallComponents.get(CouponGateway.class), mallComponents.get(CouponTemplateGateway.class), orderRpc, failing, bridge, new MemberValidationService(id -> new MemberSnapshot(id, 1))), mallSource), orderRpc);
        assertThrows(IllegalStateException.class, () -> failedPlacement.submitSettlement(1L, value.id(), "S1", settlements));
        assertEquals(0, count("trade_order"));
        assertEquals(0, cacheStore.submissions.size());

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
        new SettlementService(mallComponents.get(SettlementRecordService.class), orderRpc, placement, cache);
        assertEquals(order.orderNo(), service.findByRequest(1L, "RECOVER").orderNo());
        assertNull(service.findByRequest(2L, "RECOVER"));
        assertNull(service.findByRequest(1L, "MISSING"));
        Mockito.verifyNoInteractions(cache);
    }

    @Test
    void legacyRequestRecoveryDoesNotCreateAnotherOrder() throws Exception {
        var order = place("LEGACY-RECOVER", null);
        var service =
        new SettlementService(mallComponents.get(SettlementRecordService.class), orderRpc, placement, settlements);
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
        cartService.saveCart(1L, sku, 3, true);
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
        assertEquals(1, cacheStore.abandoned.size());
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
        assertEquals("PENDING_PAYMENT", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals(0, cacheStore.abandoned.size());
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
                assertEquals(1, cacheStore.abandoned.size());
            } else {
                assertNotNull(result);
                assertEquals(order.orderNo(), result.orderNo());
                assertEquals(1, count("trade_order"));
                assertEquals(0, cacheStore.abandoned.size());
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
        var gateway = Mockito.mock(com.lemnt.mall.domain.settlement.gateway.SettlementGateway.class,
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
        var stock = productSkuGateway.findById(sku).stock();
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
        assertEquals(stock, productSkuGateway.findById(sku).stock());
    }

    @Test
    void publicClaimDefaultsClosedAndDoesNotExposePrivateTemplates() {
        var t = campaign(2, new BigDecimal("2.00"));
        assertThrows(
                com.lemnt.common.core.exception.ServiceException.class,
                () ->
                coupons.claim(
                new CouponClaimCmd(
                1L, t.id())));
        assertEquals(
                0,
                coupons.offers(new CouponPageQry(1L, 1, 20))
                .total());
        coupons.publicClaim(
                new CouponPublicClaimCmd(t.id(), true));
        assertEquals(
                1,
                coupons.offers(new CouponPageQry(1L, 1, 20))
                .total());
        var c = coupons.claim(new CouponClaimCmd(1L, t.id()));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> coupons.claim(new CouponClaimCmd(1L, t.id())));
        assertEquals(1, couponTemplateGateway.template(t.id()).issuedCount());
        coupons.publicClaim(
                new CouponPublicClaimCmd(t.id(), false));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> coupons.claim(new CouponClaimCmd(1L, t.id())));
        assertThrows(
                com.lemnt.common.core.exception.ServiceException.class,
                () ->
                coupons.claim(
                new CouponClaimCmd(
                2L, t.id())));
        var wallet =
        coupons.wallet(new CouponPageQry(1L, 1, 20));
        assertEquals(1, wallet.total());
        assertTrue(wallet.records().get(0).usable());
        assertNotNull(wallet.records().get(0).terms());
        assertEquals(
                0,
                coupons.wallet(new CouponPageQry(2L, 1, 20))
                .total());
    }

    @Test
    void publicClaimConcurrentQuotaHasExactlyOneWinner() throws Exception {
        var t = campaign(1, BigDecimal.ONE);
        coupons.publicClaim(
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
                        coupons.claim(
                                new com.lemnt.mall.application.coupon.cmd
                                .CouponClaimCmd(member, t.id()));
                        return true;
                    } catch (com.lemnt.common.core.exception.ServiceException ex) {
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
    void templateExpiryAndDisableBlockClaimsButPreserveIssuedCoupon() throws Exception {
        var t = campaign(3, BigDecimal.ONE);
        coupons.publicClaim(
                new CouponPublicClaimCmd(t.id(), true));
        coupons.claim(new CouponClaimCmd(1L, t.id()));
        couponTemplateGateway.enableTemplate(t.id(), false);
        assertEquals(
                0,
                coupons.offers(new CouponPageQry(1L, 1, 20))
                .total());
        assertTrue(
                coupons.wallet(new CouponPageQry(1L, 1, 20))
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
                com.lemnt.common.core.exception.ServiceException.class,
                () ->
                coupons.claim(
                new CouponClaimCmd(
                2L, t.id())));
        assertTrue(
                coupons.wallet(new CouponPageQry(1L, 1, 20))
                .records()
                .get(0)
                .usable());
    }

    /** 最终数量重复设置不累加，不依赖请求记录表。 */
    @Test
    void cartQuantityIsReplacedAndRepeatedSaveDoesNotAccumulate() {
        var cmd = new CartSaveCmd(1L, sku, 3, true);
        assertEquals(3, cartAdditions.execute(cmd).quantity());
        assertEquals(3, cartAdditions.execute(cmd).quantity());
        assertEquals(3, cartGateway.cart(1L).get(0).quantity());
        assertEquals(2, cartAdditions.execute(new CartSaveCmd(1L, sku, 2, true)).quantity());
        assertEquals(3, cartAdditions.execute(new CartSaveCmd(2L, sku, 3, true)).quantity());
        assertEquals(2, cartGateway.cart(1L).get(0).quantity());
    }

    /** 超库存写入失败后，原购物车数量保持不变。 */
    @Test
    void cartQuantityRejectsStockOverflowWithoutChangingCart() {
        assertThrows(IllegalStateException.class, () -> cartAdditions.execute(new CartSaveCmd(1L, sku, 11, true)));
        assertEquals(2, cartGateway.cart(1L).get(0).quantity());
        assertEquals(3, cartAdditions.execute(new CartSaveCmd(1L, sku, 3, true)).quantity());
    }

    /** 并发设置相同最终数量不会累加。 */
    @Test
    void concurrentSameCartQuantityDoesNotAccumulate() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> task = () -> cartAdditions.execute(new CartSaveCmd(1L, sku, 3, true)).quantity();
            var first = pool.submit(task);
            var second = pool.submit(task);
            assertEquals(3, first.get(10, TimeUnit.SECONDS));
            assertEquals(3, second.get(10, TimeUnit.SECONDS));
            assertEquals(3, cartGateway.cart(1L).get(0).quantity());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void couponInsertFailureRollsBackQuotaThenRetrySucceeds() throws Exception {
        var t = campaign(3, BigDecimal.ONE);
        coupons.publicClaim(
                new CouponPublicClaimCmd(t.id(), true));
        try (var st = mallSchema.createStatement()) {
            st.execute("ALTER TABLE mall_coupon ADD CONSTRAINT reject_claim CHECK(member_id<2)");
        }
        var c = new CouponClaimCmd(2L, t.id());
        assertThrows(RuntimeException.class, () -> coupons.claim(c));
        assertEquals(0, couponTemplateGateway.template(t.id()).issuedCount());
        try (var st = mallSchema.createStatement()) {
            st.execute("ALTER TABLE mall_coupon DROP CONSTRAINT reject_claim");
        }
        assertEquals(2L, coupons.claim(c).memberId());
        assertEquals(1, couponTemplateGateway.template(t.id()).issuedCount());
    }

    @Test
    void distinctConcurrentCartQuantitiesReplaceInsteadOfAccumulate() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            var a =
            pool.submit(
                    () -> {
                start.await();
                return cartAdditions.execute(
                        new CartSaveCmd(
                        1L, sku, 2, true));
            });
            var b =
            pool.submit(
                    () -> {
                start.await();
                return cartAdditions.execute(
                        new CartSaveCmd(
                        1L, sku, 3, true));
            });
            start.countDown();
            a.get(10, TimeUnit.SECONDS);
            b.get(10, TimeUnit.SECONDS);
            assertTrue(java.util.Set.of(2, 3).contains(cartGateway.cart(1L).get(0).quantity()));
        } finally {
            pool.shutdownNow();
        }
    }
    private OrderPlacementService placementWith(
            com.lemnt.mall.domain.settlement.gateway.SettlementGateway orderGateway,
            com.lemnt.mall.domain.settlement.gateway.SettlementGateway abandonGateway) {
        return new OrderPlacementService(tx(new com.lemnt.mall.application.settlement.service.SettlementPreparationService(mallComponents.get(com.lemnt.mall.application.promotion.service.ActivityPricingService.class), mallComponents.get(com.lemnt.mall.application.settlement.service.SettlementDeliveryService.class), mallComponents.get(CartGateway.class), mallComponents.get(MemberLockGateway.class), mallComponents.get(SettlementRecordService.class), mallComponents.get(CouponGateway.class), mallComponents.get(CouponTemplateGateway.class), orderRpc, (orderGateway != settlements ? orderGateway : abandonGateway), bridge, new MemberValidationService(id -> new MemberSnapshot(id, 1))), mallSource), orderRpc);
    }
    PaymentRefundResult refundSuccess(PaymentRefund refund) {
        return new PaymentRefundResult(refund.refundNo(), refund.paymentNo(), refund.channel(),
                refund.amount(), "SUCCESS", "CHANNEL-" + refund.refundNo());
    }

    @Test
    void afterSalePaymentRefundUsesOriginalPaymentAndSuccessfulReplayNeverCallsChannelAgain() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-1", "不再需要"));
        Mockito.when(refundClient.refund(ArgumentMatchers.any(), ArgumentMatchers.any())).thenAnswer(call -> {
            assertFalse(org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive());
            return refundSuccess(call.getArgument(0));
        });
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        var result = create.execute(afterSale.refundNo());
        assertEquals("SUCCESS", result.status());
        assertEquals(afterSale.paymentNo(), result.paymentNo());
        assertEquals(afterSale.amount(), result.amount());
        assertEquals("REFUNDED", refundRequestGateway.afterSale(afterSale.refundNo()).status());
        assertEquals("REFUNDED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals("SUCCESS", payments.findByPaymentNo(result.paymentNo()).status());
        assertEquals(result, create.execute(afterSale.refundNo()));
        assertEquals(1, count("trade_payment_refund"));
        Mockito.verify(refundClient, Mockito.times(1)).refund(ArgumentMatchers.any(), ArgumentMatchers.any());
        assertThrows(IllegalStateException.class, () -> fulfillmentService.ship(shipment(order.orderNo())));
        var payment = payments.findByPaymentNo(result.paymentNo());
        notify.execute(new VerifiedPaymentNotification(payment.paymentNo(), payment.orderNo(), payment.channel(),
                payment.channelTradeNo(), payment.amount(), payment.notifyId()));
        assertEquals("REFUNDED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        var report = tradeComponents.get(MemberPaymentStatusQueryExe.class)
                .execute(new MemberPaymentStatusQry(1L, order.orderNo()));
        assertEquals("SUCCESS", report.localPaymentStatus());
        assertEquals("REFUNDED", report.orderStatus());
    }

    @Test
    void lostRefundResponsePreservesOneRefundAndOriginalKeyIsQueriedOnRetry() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-2", "原因"));
        Mockito.when(refundClient.refund(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenThrow(new IllegalStateException("渠道响应丢失"));
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        assertThrows(IllegalStateException.class, () -> create.execute(afterSale.refundNo()));
        var original = tradeComponents.get(PaymentRefundGateway.class).findByAfterSaleNo(afterSale.refundNo());
        assertEquals("WAITING", original.status());
        assertEquals("REFUNDING", refundRequestGateway.afterSale(afterSale.refundNo()).status());
        assertTrue(refundRequestGateway.activeRefund(order.orderNo()));
        assertThrows(IllegalStateException.class, () -> fulfillmentService.ship(shipment(order.orderNo())));
        assertThrows(IllegalStateException.class,
                () -> rejectAfterSale(new RefundRejectCmd(afterSale.refundNo(), "驳回")));
        Mockito.when(refundClient.query(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenAnswer(call -> refundSuccess(call.getArgument(0)));
        var result = create.execute(afterSale.refundNo());
        assertEquals(original.refundNo(), result.refundNo());
        assertEquals("SUCCESS", result.status());
        assertEquals(1, count("trade_payment_refund"));
        Mockito.verify(refundClient, Mockito.times(1)).refund(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void acceptedRefundIsNeverResubmittedWhenChannelQueryTemporarilyCannotFindIt() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-3", "原因"));
        Mockito.when(refundClient.refund(ArgumentMatchers.any(), ArgumentMatchers.any())).thenAnswer(call -> {
            PaymentRefund refund = call.getArgument(0);
            return new PaymentRefundResult(refund.refundNo(), refund.paymentNo(), refund.channel(),
                    refund.amount(), "WAITING", "ACCEPTED");
        });
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        var pending = create.execute(afterSale.refundNo());
        assertEquals(pending, create.execute(afterSale.refundNo()));
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        Mockito.verify(refundClient, Mockito.times(1)).refund(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void invalidRefundBindingNeverMarksSuccessOrReleasesFulfillmentBlock() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-4", "原因"));
        Mockito.when(refundClient.refund(ArgumentMatchers.any(), ArgumentMatchers.any())).thenAnswer(call -> {
            PaymentRefund refund = call.getArgument(0);
            return new PaymentRefundResult(refund.refundNo(), refund.paymentNo(), refund.channel(),
                    BigDecimal.ONE, "SUCCESS", "WRONG-AMOUNT");
        });
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        assertThrows(IllegalStateException.class, () -> create.execute(afterSale.refundNo()));
        var local = tradeComponents.get(PaymentRefundGateway.class).findByAfterSaleNo(afterSale.refundNo());
        assertEquals("WAITING", local.status());
        assertEquals("REFUNDING", refundRequestGateway.afterSale(afterSale.refundNo()).status());
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void refundResultAndAfterSaleAndOrderUpdatesRollbackTogether() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-5", "原因"));
        Mockito.when(refundClient.refund(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenAnswer(call -> refundSuccess(call.getArgument(0)));
        sql(tradeSource, "ALTER TABLE trade_order ADD CONSTRAINT test_refund_failure CHECK(status <> 'REFUNDED')");
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        assertThrows(RuntimeException.class, () -> create.execute(afterSale.refundNo()));
        assertEquals("WAITING", tradeComponents.get(PaymentRefundGateway.class)
                .findByAfterSaleNo(afterSale.refundNo()).status());
        assertEquals("REFUNDING", refundRequestGateway.afterSale(afterSale.refundNo()).status());
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        sql(tradeSource, "ALTER TABLE trade_order DROP CONSTRAINT test_refund_failure");
        Mockito.when(refundClient.query(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenAnswer(call -> refundSuccess(call.getArgument(0)));
        assertEquals("SUCCESS", create.execute(afterSale.refundNo()).status());
    }

    @Test
    void rejectedOrMissingAfterSaleAndMismatchedPaymentCannotCreateRefund() throws Exception {
        var order = paidOrder(null);
        var afterSale = requestAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-6", "原因"));
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        assertThrows(IllegalArgumentException.class, () -> create.execute("missing"));
        sql(tradeSource, "UPDATE trade_payment SET amount=0.01 WHERE payment_no='" + afterSale.paymentNo() + "'");
        assertThrows(IllegalStateException.class, () -> create.execute(afterSale.refundNo()));
        sql(tradeSource, "UPDATE trade_payment SET amount=" + afterSale.amount()
                + " WHERE payment_no='" + afterSale.paymentNo() + "'");
        rejectAfterSale(new RefundRejectCmd(afterSale.refundNo(), "拒绝退款"));
        assertThrows(IllegalStateException.class, () -> create.execute(afterSale.refundNo()));
        assertEquals(0, count("trade_payment_refund"));
        Mockito.verifyNoInteractions(refundClient);
    }

    @Test
    void concurrentRefundCallsUseOneDurableRefundNumber() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-7", "原因"));
        var numbers = ConcurrentHashMap.<String>newKeySet();
        Mockito.when(refundClient.refund(ArgumentMatchers.any(), ArgumentMatchers.any())).thenAnswer(call -> {
            PaymentRefund refund = call.getArgument(0);
            numbers.add(refund.refundNo());
            return refundSuccess(refund);
        });
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(() -> create.execute(afterSale.refundNo()));
            var second = pool.submit(() -> create.execute(afterSale.refundNo()));
            assertEquals(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }
        assertEquals(1, numbers.size());
        assertEquals(1, count("trade_payment_refund"));
    }

    @Test
    void paymentAndRefundPagesUseRealTableFiltersAndCommonPagination() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-8", "原因"));
        Mockito.when(refundClient.refund(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenAnswer(call -> refundSuccess(call.getArgument(0)));
        var created = tradeComponents.get(PaymentRefundCreateCmdExe.class).execute(afterSale.refundNo());
        var paymentPage = tradeComponents.get(PaymentPageQueryExe.class)
                .execute(new PaymentPageQry(null, order.orderNo(), "ALIPAY", "SUCCESS", 1, 1));
        assertEquals(1, paymentPage.getTotal());
        assertEquals(afterSale.paymentNo(), paymentPage.getRecords().get(0).paymentNo());
        assertEquals(0, tradeComponents.get(PaymentPageQueryExe.class)
                .execute(new PaymentPageQry(null, order.orderNo(), "WECHAT", null, 1, 20)).getTotal());
        var refundQuery = tradeComponents.get(PaymentRefundPageQueryExe.class);
        var refundPage = refundQuery.execute(new PaymentRefundPageQry(afterSale.refundNo(), created.paymentNo(), "SUCCESS", 1, 20));
        assertEquals(1, refundPage.getTotal());
        assertEquals(created, refundPage.getRecords().get(0));
        assertEquals(0, refundQuery.execute(new PaymentRefundPageQry(afterSale.refundNo(), null, "FAILED", 1, 20)).getTotal());
        var afterSalePage = new AfterSalePageQueryExe(refundRequestGateway)
                .execute(new AfterSalePageQry(order.orderNo(), "REFUNDED", 1, 20));
        assertEquals(1, afterSalePage.getTotal());
        var orderPage = tradeComponents.get(OrderPageQueryExe.class)
                .execute(new OrderPageQry(order.orderNo(), "REFUNDED", 1, 20));
        assertEquals(1, orderPage.total());
    }

    @Test
    void callerRollbackDoesNotEraseChannelRefundOrItsCommittedResult() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-9", "原因"));
        Mockito.when(refundClient.refund(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenAnswer(call -> refundSuccess(call.getArgument(0)));
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        var transaction = new org.springframework.transaction.support.TransactionTemplate(
                new DataSourceTransactionManager(tradeSource));
        transaction.executeWithoutResult(status -> {
            assertEquals("SUCCESS", create.execute(afterSale.refundNo()).status());
            status.setRollbackOnly();
        });
        assertEquals("SUCCESS", tradeComponents.get(PaymentRefundGateway.class)
                .findByAfterSaleNo(afterSale.refundNo()).status());
        assertEquals("REFUNDED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void confirmedChannelFailureIsRecordedWithoutPretendingOrderWasRefunded() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-10", "原因"));
        Mockito.when(refundClient.refund(ArgumentMatchers.any(), ArgumentMatchers.any())).thenAnswer(call -> {
            PaymentRefund refund = call.getArgument(0);
            return new PaymentRefundResult(refund.refundNo(), refund.paymentNo(), refund.channel(),
                    refund.amount(), "FAILED", null);
        });
        var result = tradeComponents.get(PaymentRefundCreateCmdExe.class).execute(afterSale.refundNo());
        assertEquals("FAILED", result.status());
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals("REFUNDING", refundRequestGateway.afterSale(afterSale.refundNo()).status());
        assertTrue(refundRequestGateway.activeRefund(order.orderNo()));
    }

    AfterSaleCreateCmd itemRefund(String orderNo, String requestNo, int quantity, String type) {
        var line = tradeComponents.get(AfterSaleService.class).items(orderNo).get(0);
        return new AfterSaleCreateCmd(orderNo, requestNo, line.orderItemId(), quantity, type, "商品售后");
    }

    void successfulRefundChannel() {
        Mockito.when(refundClient.refund(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenAnswer(call -> refundSuccess(call.getArgument(0)));
    }

    @Test
    void partialRefundsUseDiscountedItemQuotaAndOnlyCumulativeFullRefundClosesOrder() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, new BigDecimal("3.00")).id());
        var order = paidOrder(coupon.id());
        var service = tradeComponents.get(AfterSaleService.class);
        var first = service.create(itemRefund(order.orderNo(), "PARTIAL-1", 1, "REFUND_ONLY"));
        assertEquals(new BigDecimal("8.50"), first.amount());
        assertEquals(first, service.create(itemRefund(order.orderNo(), "PARTIAL-1", 1, "REFUND_ONLY")));
        var createRefund = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        assertThrows(IllegalStateException.class, () -> createRefund.execute(first.refundNo()));
        service.agree(first.refundNo());
        sql(tradeSource, "UPDATE trade_after_sale SET decided_at='2026-01-01 00:00:00' WHERE refund_no='"
                + first.refundNo() + "'");
        successfulRefundChannel();
        assertEquals("SUCCESS", createRefund.execute(first.refundNo()).status());
        assertEquals(java.time.LocalDateTime.of(2026, 1, 1, 0, 0),
                refundRequestGateway.afterSale(first.refundNo()).decidedAt());
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals(1, service.items(order.orderNo()).get(0).refundableQuantity());
        assertThrows(IllegalStateException.class, () -> approvedAfterSale(
                new RefundRequestCmd(order.orderNo(), "LEGACY-FULL", "不能重复整单退")));
        var second = service.create(itemRefund(order.orderNo(), "PARTIAL-2", 1, "REFUND_ONLY"));
        assertEquals(new BigDecimal("8.50"), second.amount());
        service.agree(second.refundNo());
        createRefund.execute(second.refundNo());
        assertEquals("REFUNDED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals(0, service.items(order.orderNo()).get(0).refundableQuantity());
        assertEquals(2, count("trade_payment_refund"));
    }

    @Test
    void returnedGoodsMustBeApprovedRegisteredAndReceivedBeforeFinancialRefund() throws Exception {
        var order = paidOrder(null);
        var service = tradeComponents.get(AfterSaleService.class);
        assertThrows(IllegalStateException.class,
                () -> service.create(itemRefund(order.orderNo(), "RETURN-BEFORE-SHIP", 1, "RETURN_REFUND")));
        fulfillmentService.ship(shipment(order.orderNo()));
        var sale = service.create(itemRefund(order.orderNo(), "RETURN-1", 1, "RETURN_REFUND"));
        var refund = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        assertEquals("AWAITING_RETURN", service.agree(sale.refundNo()).status());
        assertThrows(IllegalStateException.class, () -> refund.execute(sale.refundNo()));
        var registered = service.registerReturn(new AfterSaleReturnCmd(sale.refundNo(), "SF", "RET-1"));
        assertEquals("RETURNED", registered.status());
        assertEquals(registered, service.registerReturn(new AfterSaleReturnCmd(sale.refundNo(), "SF", "RET-1")));
        assertThrows(IllegalStateException.class,
                () -> service.registerReturn(new AfterSaleReturnCmd(sale.refundNo(), "SF", "OTHER")));
        assertThrows(IllegalStateException.class, () -> refund.execute(sale.refundNo()));
        var received = service.receive(sale.refundNo());
        assertEquals("WAIT_REFUND", received.status());
        assertNotNull(received.returnReceivedAt());
        assertEquals(received, service.receive(sale.refundNo()));
        successfulRefundChannel();
        assertEquals("SUCCESS", refund.execute(sale.refundNo()).status());
        assertEquals("SHIPPED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void rejectedOrRefusedAfterSalesRestoreItemQuotaAndCannotInitiateRefund() throws Exception {
        var order = paidOrder(null);
        var service = tradeComponents.get(AfterSaleService.class);
        var first = service.create(itemRefund(order.orderNo(), "REJECT-ITEM", 1, "REFUND_ONLY"));
        var rejected = service.reject(new AfterSaleRejectCmd(first.refundNo(), "拒绝原因"));
        assertEquals(rejected, service.reject(new AfterSaleRejectCmd(first.refundNo(), "拒绝原因")));
        assertEquals(2, service.items(order.orderNo()).get(0).refundableQuantity());
        assertThrows(IllegalStateException.class,
                () -> tradeComponents.get(PaymentRefundCreateCmdExe.class).execute(first.refundNo()));
        fulfillmentService.ship(shipment(order.orderNo()));
        var returned = service.create(itemRefund(order.orderNo(), "REFUSE-ITEM", 1, "RETURN_REFUND"));
        service.agree(returned.refundNo());
        service.registerReturn(new AfterSaleReturnCmd(returned.refundNo(), "SF", "RET-REFUSE"));
        var refused = service.refuse(new AfterSaleRefuseCmd(returned.refundNo(), "商品不符"));
        assertEquals("RETURN_REJECTED", refused.status());
        assertEquals(2, service.items(order.orderNo()).get(0).refundableQuantity());
        assertThrows(IllegalStateException.class, () -> service.receive(returned.refundNo()));
        assertThrows(IllegalStateException.class,
                () -> tradeComponents.get(PaymentRefundCreateCmdExe.class).execute(returned.refundNo()));
        Mockito.verifyNoInteractions(refundClient);
    }

    @Test
    void concurrentItemRequestsNeverReserveMoreThanPurchasedQuantity() throws Exception {
        var order = paidOrder(null);
        var service = tradeComponents.get(AfterSaleService.class);
        var cmd1 = itemRefund(order.orderNo(), "CONCURRENT-A", 2, "REFUND_ONLY");
        var cmd2 = itemRefund(order.orderNo(), "CONCURRENT-B", 2, "REFUND_ONLY");
        var pool = Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.Callable<Boolean> a = () -> {
                try { service.create(cmd1); return true; } catch (IllegalStateException expected) { return false; }
            };
            java.util.concurrent.Callable<Boolean> b = () -> {
                try { service.create(cmd2); return true; } catch (IllegalStateException expected) { return false; }
            };
            int success = 0;
            for (var result : pool.invokeAll(List.of(a, b))) {
                if (result.get(10, TimeUnit.SECONDS)) { success++; }
            }
            assertEquals(1, success);
        } finally {
            pool.shutdownNow();
        }
        assertEquals(1, count("trade_after_sale"));
    }

    @Test
    void itemOwnershipRequestBindingAndApprovedRefundBlockAreConsistentAcrossOrderReads() throws Exception {
        var first = paidOrder(null);
        var second = placement.createDirect(new SettlementDirectCmd(1L,
                List.of(new SettlementDirectCmd.OrderLine(sku, 1)), "ANOTHER", null));
        var service = tradeComponents.get(AfterSaleService.class);
        var foreign = service.items(second.orderNo()).get(0);
        assertThrows(IllegalArgumentException.class, () -> service.create(new AfterSaleCreateCmd(first.orderNo(),
                "FOREIGN-ITEM", foreign.orderItemId(), 1, "REFUND_ONLY", "错误商品")));
        var sale = service.create(itemRefund(first.orderNo(), "BINDING", 1, "REFUND_ONLY"));
        var changed = itemRefund(first.orderNo(), "BINDING", 2, "REFUND_ONLY");
        assertThrows(IllegalStateException.class, () -> service.create(changed));
        service.agree(sale.refundNo());
        var page = tradeComponents.get(OrderPageQueryExe.class).execute(new OrderPageQry(first.orderNo(), null, 1, 20));
        assertTrue(page.records().get(0).activeRefund());
        assertThrows(IllegalStateException.class, () -> fulfillmentService.ship(shipment(first.orderNo())));
        assertThrows(IllegalStateException.class, () -> service.reject(new AfterSaleRejectCmd(sale.refundNo(), "不能倒退")));
    }

    @Test
    void shipmentPagesFilterOnlyRealPersistedWaybillsAndReadDoesNotChangeReceipt() throws Exception {
        var order = paidOrder(null);
        fulfillmentService.ship(shipment(order.orderNo()));
        var query = tradeComponents.get(ShipmentPageQueryExe.class);
        // 真实登记的运单按实际值筛选，不使用调用方伪造快递号。
        var stored = shipmentGateway.shipment(order.orderNo());
        var pending = query.execute(new ShipmentPageQry(order.orderNo(), stored.carrierCode(), stored.trackingNo(), "SHIPPED", 1, 20));
        assertEquals(1, pending.getTotal());
        assertEquals(0, query.execute(new ShipmentPageQry(order.orderNo(), null, "OTHER", null, 1, 20)).getTotal());
        var detail = tradeComponents.get(ShipmentDetailQueryExe.class).execute(order.orderNo());
        assertEquals(stored.phone(), detail.phone());
        assertNull(detail.receivedAt());
        fulfillmentService.receive(order.orderNo());
        assertEquals(1, query.execute(new ShipmentPageQry(order.orderNo(), null, null, "RECEIVED", 1, 20)).getTotal());
        assertEquals(0, query.execute(new ShipmentPageQry(order.orderNo(), null, null, "SHIPPED", 1, 20)).getTotal());
    }

    @Test
    void partialRefundRoundingRejectionAndFinalCentNeverExceedRealPaidAmount() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, new BigDecimal("10.01")).id());
        var order = placement.createDirect(new SettlementDirectCmd(1L,
                List.of(new SettlementDirectCmd.OrderLine(sku, 3)), "ROUNDING", coupon.id()));
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        notify.execute(new VerifiedPaymentNotification(payment.paymentNo(), order.orderNo(), "ALIPAY",
                "ROUNDING-CH", payment.amount(), "ROUNDING-NOTICE"));
        assertEquals(new BigDecimal("19.99"), payment.amount());
        var service = tradeComponents.get(AfterSaleService.class);
        var a = service.create(itemRefund(order.orderNo(), "ROUND-A", 1, "REFUND_ONLY"));
        var b = service.create(itemRefund(order.orderNo(), "ROUND-B", 1, "REFUND_ONLY"));
        service.reject(new AfterSaleRejectCmd(a.refundNo(), "释放额度"));
        var c = service.create(itemRefund(order.orderNo(), "ROUND-C", 1, "REFUND_ONLY"));
        var d = service.create(itemRefund(order.orderNo(), "ROUND-D", 1, "REFUND_ONLY"));
        assertEquals(new BigDecimal("19.99"), b.amount().add(c.amount()).add(d.amount()));
        assertEquals(new BigDecimal("6.67"), d.amount());
        successfulRefundChannel();
        for (var sale : List.of(b, c, d)) {
            service.agree(sale.refundNo());
            tradeComponents.get(PaymentRefundCreateCmdExe.class).execute(sale.refundNo());
        }
        assertEquals("REFUNDED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals(0, service.items(order.orderNo()).get(0).refundableQuantity());
        assertThrows(IllegalStateException.class,
                () -> service.create(itemRefund(order.orderNo(), "ROUND-EXCESS", 1, "REFUND_ONLY")));
    }

    @Test
    void orderStatisticsCountOnlySuccessfulReverseTransactionsAndRemarksCanBeCleared() throws Exception {
        var order = paidOrder(null);
        var statistics = tradeComponents.get(com.lemnt.trade.application.order.executor.OrderStatisticsQueryExe.class);
        var initial = statistics.execute(order.orderNo());
        assertEquals(1, initial.orderCount());
        assertEquals(0, initial.afterSaleCount());
        assertEquals(0, initial.afterSalePrice().signum());
        var sale = tradeComponents.get(AfterSaleService.class)
                .create(itemRefund(order.orderNo(), "STATS-PARTIAL", 1, "REFUND_ONLY"));
        assertEquals(0, statistics.execute(order.orderNo()).afterSaleCount());
        tradeComponents.get(AfterSaleService.class).agree(sale.refundNo());
        successfulRefundChannel();
        tradeComponents.get(PaymentRefundCreateCmdExe.class).execute(sale.refundNo());
        var result = statistics.execute(order.orderNo());
        assertEquals(1, result.afterSaleCount());
        assertEquals(new BigDecimal("10.00"), result.afterSalePrice());
        assertEquals(new BigDecimal("20.00"), result.orderPayPrice());
        var remark = tradeComponents.get(com.lemnt.trade.application.order.executor.OrderRemarkUpdateCmdExe.class);
        remark.execute(new com.lemnt.trade.application.order.cmd.OrderRemarkUpdateCmd(order.orderNo(), "联系买家"));
        assertEquals("联系买家", tradeOrderGateway.remark(order.orderNo()));
        remark.execute(new com.lemnt.trade.application.order.cmd.OrderRemarkUpdateCmd(order.orderNo(), ""));
        assertEquals("", tradeOrderGateway.remark(order.orderNo()));
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void newAfterSaleRejectionAlsoReleasesLegacyWholeOrderUniquePlaceholder() throws Exception {
        var order = paidOrder(null);
        var original = requestAfterSale(new RefundRequestCmd(order.orderNo(), "OLD-1", "旧整单售后"));
        tradeComponents.get(AfterSaleService.class).reject(new AfterSaleRejectCmd(original.refundNo(), "拒绝"));
        var next = requestAfterSale(new RefundRequestCmd(order.orderNo(), "OLD-2", "再次申请"));
        assertNotEquals(original.refundNo(), next.refundNo());
        assertEquals("REQUESTED", next.status());
    }

    void useCatalogSkuForReview() {
        var category = mallComponents.get(com.lemnt.mall.domain.product.gateway.CategoryGateway.class)
                .saveCategory(new com.lemnt.mall.domain.product.model.Category(null, null, "评价分类", null, 0, "ENABLED"));
        var brand = mallComponents.get(com.lemnt.mall.domain.product.gateway.BrandGateway.class)
                .saveBrand(new com.lemnt.mall.domain.product.model.Brand(null, "评价品牌", null, null, 0, "ENABLED"));
        var product = mallComponents.get(com.lemnt.mall.application.product.service.ProductCatalogService.class)
                .createSpu(com.lemnt.mall.domain.product.model.ProductCatalogValidation.newSpu(
                        new com.lemnt.mall.domain.product.model.ProductSpu(null, "COMMENT-SPU", "评价商品", category.id(),
                                brand.id(), null, null, null, List.of(new com.lemnt.mall.domain.product.model.ProductSku(
                                        null, null, "COMMENT-SKU", "评价SKU", BigDecimal.TEN, 10, null, Map.of(), null)))));
        mallComponents.get(com.lemnt.mall.domain.product.gateway.ProductSpuGateway.class).shelf(product.id(), true);
        sku = product.skus().get(0).id();
        cartService.saveCart(1L, sku, 2, true);
    }

    @Test
    void commentsRequireActualOwnedCompletedOrderAndCannotForgeBuyerOrProduct() throws Exception {
        useCatalogSkuForReview();
        var order = paidOrder(null);
        var comments = mallComponents.get(ProductCommentService.class);
        var cmd = new CommentCreateCmd(1L, order.orderNo(), sku, 5, "真实评价");
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> comments.create(cmd));
        fulfillmentService.ship(shipment(order.orderNo()));
        fulfillmentService.receive(order.orderNo());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> comments.create(new CommentCreateCmd(2L, order.orderNo(), sku, 5, "他人订单")));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> comments.create(new CommentCreateCmd(1L, order.orderNo(), sku + 999, 5, "错误商品")));
        var saved = comments.create(cmd);
        assertEquals(1L, saved.memberId());
        assertEquals(saved, comments.create(cmd));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> comments.create(new CommentCreateCmd(1L, order.orderNo(), sku, 1, "更改原评价")));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> comments.create(new CommentCreateCmd(1L, order.orderNo(), sku, 6, "错误评分")));
    }

    @Test
    void merchantReplyAndVisibilityKeepBuyerRatingAndFilteringUsesRealCommentRows() throws Exception {
        useCatalogSkuForReview();
        var order = paidOrder(null);
        fulfillmentService.ship(shipment(order.orderNo()));
        fulfillmentService.receive(order.orderNo());
        var comments = mallComponents.get(ProductCommentService.class);
        var saved = comments.create(new CommentCreateCmd(1L, order.orderNo(), sku, 5, "已完成订单评价"));
        var replied = comments.reply(new CommentReplyCmd(saved.id(), 99L, "感谢反馈"));
        assertEquals(99L, replied.replyUserId());
        assertEquals(replied, comments.reply(new CommentReplyCmd(saved.id(), 99L, "感谢反馈")));
        mallComponents.get(CommentVisibleCmdExe.class).execute(new CommentVisibleCmd(saved.id(), false));
        var query = mallComponents.get(CommentPageQueryExe.class);
        var page = query.execute(new CommentPageQry(saved.spuId(), 1L, 5, false, true, 1, 20));
        assertEquals(1, page.getTotal());
        assertEquals("已完成订单评价", page.getRecords().get(0).content());
        assertEquals("感谢反馈", page.getRecords().get(0).replyContent());
        assertEquals(0, query.execute(new CommentPageQry(saved.spuId(), 1L, 5, true, null, 1, 20)).getTotal());
    }

    @Test
    void concurrentReviewSubmissionsReturnOneStableIdForTheSameOrderSku() throws Exception {
        useCatalogSkuForReview();
        var order = paidOrder(null);
        fulfillmentService.ship(shipment(order.orderNo()));
        fulfillmentService.receive(order.orderNo());
        var service = mallComponents.get(ProductCommentService.class);
        var cmd = new CommentCreateCmd(1L, order.orderNo(), sku, 4, "重复提交只保存一次");
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> service.create(cmd));
            var b = pool.submit(() -> service.create(cmd));
            assertEquals(a.get(10, TimeUnit.SECONDS).id(), b.get(10, TimeUnit.SECONDS).id());
        } finally { pool.shutdownNow(); }
        assertNotNull(mallComponents.get(ProductCommentGateway.class).findByOrderItem(orderRpc.findByOrderNo(order.orderNo()).items().stream().filter(item -> item.productId().equals(sku)).findFirst().orElseThrow().orderItemId()));
    }

    @Test
    void deletedCatalogStillRetainsCompletedPurchaseAndCommentHistory() throws Exception {
        useCatalogSkuForReview();
        var order = paidOrder(null);
        fulfillmentService.ship(shipment(order.orderNo()));
        fulfillmentService.receive(order.orderNo());
        var parentId = productSkuGateway.catalogSku(sku).spuId();
        var management = mallComponents.get(ProductManagementService.class);
        mallComponents.get(com.lemnt.mall.domain.product.gateway.ProductSpuGateway.class).shelf(parentId, false);
        management.recycle(parentId);
        management.delete(parentId);
        var saved = mallComponents.get(ProductCommentService.class)
                .create(new CommentCreateCmd(1L, order.orderNo(), sku, 5, "历史订单仍可反馈"));
        assertEquals(parentId, saved.spuId());
        assertEquals("DELETED", productSkuGateway.catalogSku(sku).status());
        assertEquals("COMPLETED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    /** 旧用例的数据准备改走商品售后，以第一个商品的全部数量申请。 */
    private com.lemnt.trade.domain.fulfillment.model.RefundRequest requestAfterSale(RefundRequestCmd cmd) {
        var order = orderPersistenceService.findByOrderNo(cmd.orderNo());
        if (order == null) { throw new IllegalArgumentException("订单不存在"); }
        var line = tradeComponents.get(com.lemnt.trade.domain.order.gateway.TradeOrderItemGateway.class)
                .refundItems(order.id()).get(0);
        var sale = tradeComponents.get(com.lemnt.trade.application.aftersale.service.AfterSaleService.class)
                .create(new AfterSaleCreateCmd(cmd.orderNo(), cmd.requestNo(), line.id(), line.quantity(),
                        "REFUND_ONLY", cmd.reason()));
        return legacyView(sale);
    }

    private com.lemnt.trade.domain.fulfillment.model.RefundRequest rejectAfterSale(RefundRejectCmd cmd) {
        return legacyView(tradeComponents.get(com.lemnt.trade.application.aftersale.service.AfterSaleService.class)
                .reject(new com.lemnt.trade.application.aftersale.cmd.AfterSaleRejectCmd(cmd.refundNo(), cmd.reason())));
    }

    private com.lemnt.trade.domain.fulfillment.model.RefundRequest legacyView(
            com.lemnt.trade.domain.aftersale.model.AfterSale sale) {
        return new com.lemnt.trade.domain.fulfillment.model.RefundRequest(sale.refundNo(), sale.orderNo(),
                sale.requestNo(), sale.memberId(), sale.paymentNo(), sale.amount(), sale.reason(), sale.status(),
                sale.decisionReason(), sale.requestedAt(), sale.decidedAt());
    }
    private com.lemnt.trade.domain.fulfillment.model.RefundRequest approvedAfterSale(RefundRequestCmd cmd) {
        var requested = requestAfterSale(cmd);
        return legacyView(tradeComponents.get(com.lemnt.trade.application.aftersale.service.AfterSaleService.class)
                .agree(requested.refundNo()));
    }

    /** 真正业务状态变化才生成日志，重放不重复记录。 */
    @Test
    void orderLogsTrackCreatePayShipAndMemberReceiveExactlyOnce() {
        var order = paidOrder(null);
        var command = shipment(order.orderNo());
        var operator = com.lemnt.trade.domain.order.model.TradeOperator.administrator(55L);
        var shipping = new ShipmentCreateCmd(command.orderNo(), command.requestNo(), command.carrierCode(), command.trackingNo(),
                command.recipientName(), command.phone(), command.address(), operator);
        fulfillmentService.ship(shipping);
        fulfillmentService.ship(shipping);
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> fulfillmentService.receive(order.orderNo(),
                com.lemnt.trade.domain.order.model.TradeOperator.member(2L)));
        fulfillmentService.receive(order.orderNo(), com.lemnt.trade.domain.order.model.TradeOperator.member(1L));
        fulfillmentService.receive(order.orderNo(), com.lemnt.trade.domain.order.model.TradeOperator.member(1L));
        var header = orderPersistenceService.findByOrderNo(order.orderNo());
        var logs = tradeComponents.get(com.lemnt.trade.domain.order.gateway.OrderLogGateway.class).byOrderId(header.id());
        assertEquals(List.of(1, 10, 20, 30), logs.stream().map(com.lemnt.trade.domain.order.model.OrderLog::operateType).toList());
        assertEquals(List.of(0, 10, 20, 30), logs.stream().map(com.lemnt.trade.domain.order.model.OrderLog::afterStatus).toList());
        assertEquals(55L, logs.get(2).userId());
        assertEquals(2, logs.get(2).userType());
        assertEquals(1L, logs.get(3).userId());
        assertNotNull(logs.get(3).createTime());
    }

    /** 会员取消只允许本人，库存与券的补偿仍在提交后异步发送。 */
    @Test
    void memberCancelChecksOwnerAndRepeatedCancellationDoesNotDuplicateLog() {
        var order = place("CANCEL-LOG", null);
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> closer.executeForMember(2L, order.orderNo()));
        assertTrue(closer.executeForMember(1L, order.orderNo()));
        assertTrue(closer.executeForMember(1L, order.orderNo()));
        var header = orderPersistenceService.findByOrderNo(order.orderNo());
        var logs = tradeComponents.get(com.lemnt.trade.domain.order.gateway.OrderLogGateway.class).byOrderId(header.id());
        assertEquals(List.of(1, 40), logs.stream().map(com.lemnt.trade.domain.order.model.OrderLog::operateType).toList());
        assertEquals(1L, logs.get(1).userId());
        assertEquals("CLOSED", header.status());
    }

    /** 日志写入失败必须与订单、支付状态一并回滚。 */
    @Test
    void paymentLogFailureRollsBackStateAndRetryProducesSinglePaymentLog() throws Exception {
        var order = place("LOG-FAILURE", null);
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        sql(tradeSource, "ALTER TABLE trade_order_log ADD CONSTRAINT reject_payment_log CHECK(operate_type<>10)");
        var notification = new VerifiedPaymentNotification(payment.paymentNo(), order.orderNo(), "ALIPAY",
                "LOG-PAY", order.totalAmount(), "NOTICE-LOG");
        assertThrows(RuntimeException.class, () -> notify.execute(notification));
        assertEquals("PENDING_PAYMENT", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals("WAITING", payments.findByPaymentNo(payment.paymentNo()).status());
        sql(tradeSource, "ALTER TABLE trade_order_log DROP CONSTRAINT reject_payment_log");
        notify.execute(notification);
        notify.execute(notification);
        var header = orderPersistenceService.findByOrderNo(order.orderNo());
        var logs = tradeComponents.get(com.lemnt.trade.domain.order.gateway.OrderLogGateway.class).byOrderId(header.id());
        assertEquals(List.of(1, 10), logs.stream().map(com.lemnt.trade.domain.order.model.OrderLog::operateType).toList());
    }

    /** 退货退款各阶段按实际操作者记录源状态，回调重放不重复日志。 */
    @Test
    void afterSaleReturnRefundLogsUseNumericIdentityAndRecordRealSuccessTime() {
        var order = paidOrder(null);
        fulfillmentService.ship(shipment(order.orderNo()));
        var service = tradeComponents.get(AfterSaleService.class);
        var member = com.lemnt.trade.domain.order.model.TradeOperator.member(1L);
        var admin = com.lemnt.trade.domain.order.model.TradeOperator.administrator(55L);
        var sale = service.create(itemRefund(order.orderNo(), "LOG-RETURN", 1, "RETURN_REFUND"), member);
        assertNotNull(sale.id());
        assertEquals(orderPersistenceService.findByOrderNo(order.orderNo()).id(), sale.orderId());
        service.agree(sale.refundNo(), admin);
        service.agree(sale.refundNo(), admin);
        service.registerReturn(new AfterSaleReturnCmd(sale.refundNo(), "TEST", "BACK-LOG"), member);
        service.receive(sale.refundNo(), admin);
        successfulRefundChannel();
        var refund = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        assertEquals("SUCCESS", refund.executeForAdmin(sale.refundNo(), 55L).status());
        assertEquals("SUCCESS", refund.executeForAdmin(sale.refundNo(), 55L).status());
        var logs = tradeComponents.get(com.lemnt.trade.domain.aftersale.gateway.AfterSaleLogGateway.class).byAfterSaleId(sale.id());
        assertEquals(List.of(10, 11, 20, 21, 30, 31), logs.stream().map(com.lemnt.trade.domain.aftersale.model.AfterSaleLog::operateType).toList());
        assertEquals(List.of(10, 20, 30, 40, 40, 50), logs.stream().map(com.lemnt.trade.domain.aftersale.model.AfterSaleLog::afterStatus).toList());
        assertEquals(55L, logs.get(1).userId());
        assertEquals(2, logs.get(1).userType());
        assertEquals(1L, logs.get(2).userId());
        assertEquals(0L, logs.get(5).userId());
        assertNotNull(refundRequestGateway.afterSale(sale.refundNo()).refundTime());
        assertEquals(55L, refundRequestGateway.afterSale(sale.refundNo()).auditUserId());
    }

    /** 会员取消必须先校验归属，重放也不能查看他人的记录。 */
    @Test
    void memberAfterSaleCancelRestoresQuotaAndCannotCancelRefundStage() {
        var order = paidOrder(null);
        var service = tradeComponents.get(AfterSaleService.class);
        var sale = service.create(itemRefund(order.orderNo(), "CANCEL-SALE", 1, "REFUND_ONLY"));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> service.cancel(2L, sale.refundNo()));
        service.cancel(1L, sale.refundNo());
        service.cancel(1L, sale.refundNo());
        assertEquals(2, service.items(order.orderNo()).get(0).refundableQuantity());
        var logs = tradeComponents.get(com.lemnt.trade.domain.aftersale.gateway.AfterSaleLogGateway.class).byAfterSaleId(sale.id());
        assertEquals(List.of(10, 40), logs.stream().map(com.lemnt.trade.domain.aftersale.model.AfterSaleLog::operateType).toList());
        assertEquals(61, logs.get(1).afterStatus());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> service.cancel(2L, sale.refundNo()));
        var next = service.create(itemRefund(order.orderNo(), "AFTER-CANCEL", 1, "REFUND_ONLY"));
        service.agree(next.refundNo());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> service.cancel(1L, next.refundNo()));
        assertEquals("WAIT_REFUND", refundRequestGateway.afterSale(next.refundNo()).status());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> service.cancel(1L, "MISSING-SALE"));
    }

    /** 日志失败不能提交审批；真实失败回调只记录一次且不伪造退款时间。 */
    @Test
    void afterSaleLogFailureRollsBackApprovalAndFailedRefundReplayDoesNotDuplicateLog() throws Exception {
        var order = paidOrder(null);
        var service = tradeComponents.get(AfterSaleService.class);
        var sale = service.create(itemRefund(order.orderNo(), "LOG-FAIL-SALE", 1, "REFUND_ONLY"));
        sql(tradeSource, "ALTER TABLE trade_after_sale_log ADD CONSTRAINT reject_agreement_log CHECK(operate_type<>11)");
        assertThrows(RuntimeException.class, () -> service.agree(sale.refundNo()));
        assertEquals("REQUESTED", refundRequestGateway.afterSale(sale.refundNo()).status());
        sql(tradeSource, "ALTER TABLE trade_after_sale_log DROP CONSTRAINT reject_agreement_log");
        service.agree(sale.refundNo());
        Mockito.when(refundClient.refund(ArgumentMatchers.any(), ArgumentMatchers.any())).thenAnswer(call -> {
            PaymentRefund refund = call.getArgument(0);
            return new PaymentRefundResult(refund.refundNo(), refund.paymentNo(), refund.channel(), refund.amount(), "FAILED", null);
        });
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        create.execute(sale.refundNo());
        create.execute(sale.refundNo());
        var logs = tradeComponents.get(com.lemnt.trade.domain.aftersale.gateway.AfterSaleLogGateway.class).byAfterSaleId(sale.id());
        assertEquals(List.of(10, 11, 30, 32), logs.stream().map(com.lemnt.trade.domain.aftersale.model.AfterSaleLog::operateType).toList());
        assertNull(refundRequestGateway.afterSale(sale.refundNo()).refundTime());
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }
    /** 实际商品、结算、订单落库及售后共用下单时快照，商品修改不改历史。 */
    @Test
    void originalProductSnapshotSurvivesCatalogChangesAndAfterSalePersistence() throws Exception {
        useCatalogSkuForReview();
        var spuId = productSkuGateway.catalogSku(sku).spuId();
        sql(mallSource, "UPDATE mall_product_sku SET main_image='https://example.com/old.png' WHERE id=" + sku);
        var dict = mallComponents.get(com.lemnt.mall.application.product.service.ProductSpecService.class);
        var spec = dict.create(new com.lemnt.mall.application.product.cmd.ProductSpecCreateCmd("颜色", null));
        var option = dict.createValue(new com.lemnt.mall.application.product.cmd.ProductSpecValueCreateCmd(spec.id(), "黑色", null));
        var catalog = mallComponents.get(com.lemnt.mall.application.product.service.ProductCatalogService.class);
        var originalSpu = catalog.spu(spuId);
        var originalSku = originalSpu.skus().get(0);
        var linked = new com.lemnt.mall.domain.product.model.ProductSku(originalSku.id(), spuId, originalSku.code(),
                originalSku.name(), originalSku.price(), originalSku.stock(), originalSku.image(),
                List.of(new com.lemnt.mall.domain.product.model.ProductSkuSpec(spec.id(), null, option.id(), null)),
                originalSku.status(), originalSku.weight(), originalSku.volume());
        catalog.updateSpu(new com.lemnt.mall.domain.product.model.ProductSpu(spuId, originalSpu.code(), originalSpu.name(),
                originalSpu.categoryId(), originalSpu.brandId(), originalSpu.description(), originalSpu.image(),
                originalSpu.status(), List.of(linked), originalSpu.deliveryTypes(), originalSpu.deliveryTemplateId(), originalSpu.keyword(), originalSpu.introduction(), originalSpu.sliderPicUrls(), originalSpu.sort()));
        var receipt = paidOrder(null);
        var order = orderPersistenceService.findByOrderNo(receipt.orderNo());
        var item = order.items().get(0);
        assertEquals(spuId, item.spuId());
        assertEquals("评价商品", item.spuName());
        assertEquals("颜色", item.properties().get(0).propertyName());
        assertEquals("黑色", item.properties().get(0).valueName());
        assertEquals(spec.id(), item.properties().get(0).propertyId());
        assertEquals(option.id(), item.properties().get(0).valueId());
        assertEquals("https://example.com/old.png", item.picUrl());
        var changedItem = new com.lemnt.trade.domain.order.model.TradeOrderItem(item.productId(), item.productCode(),
                item.productName(), item.unitPrice(), item.quantity(), item.lineAmount(), item.payAmount(),
                item.deliveryPrice(), item.spuId(), item.spuName(), item.properties(), "https://example.com/forged.png");
        var changedOrder = new com.lemnt.trade.domain.order.model.TradeOrder(order.id(), order.orderNo(), order.memberId(),
                order.totalAmount(), order.status(), List.of(changedItem), order.createTime(), order.usePoint(),
                order.givePoint(), order.delivery());
        assertThrows(IllegalStateException.class, () -> tradeComponents
                .get(com.lemnt.trade.application.order.service.OrderSubmissionService.class).create(changedOrder));
        sql(mallSource, "UPDATE mall_product_spu SET name='已改商品' WHERE id=" + spuId);
        sql(mallSource, "UPDATE mall_product_sku SET main_image='https://example.com/new.png' WHERE id=" + sku);
        dict.update(new com.lemnt.mall.application.product.cmd.ProductSpecUpdateCmd(spec.id(), "色彩", null));
        dict.updateValue(new com.lemnt.mall.application.product.cmd.ProductSpecValueUpdateCmd(option.id(), "白色", null));
        assertEquals("白色", catalog.sku(sku).properties().get(0).valueName());
        assertEquals(item, orderPersistenceService.findByOrderNo(order.orderNo()).items().get(0));
        var service = tradeComponents.get(AfterSaleService.class);
        var sale = service.create(itemRefund(order.orderNo(), "SNAPSHOT", 1, "REFUND_ONLY"));
        var saved = refundRequestGateway.afterSale(sale.refundNo());
        assertEquals(sku, saved.skuId());
        assertEquals(item.spuId(), saved.spuId());
        assertEquals(item.spuName(), saved.spuName());
        assertEquals(item.properties().get(0).propertyName(), saved.properties().get(0).propertyName());
        assertEquals(item.properties().get(0).valueName(), saved.properties().get(0).valueName());
        assertEquals(item.properties().get(0).propertyId(), saved.properties().get(0).propertyId());
        assertEquals(item.properties().get(0).valueId(), saved.properties().get(0).valueId());
        assertEquals(item.picUrl(), saved.picUrl());
        service.agree(saved.refundNo());
        assertEquals(saved.properties(), refundRequestGateway.afterSale(saved.refundNo()).properties());
    }

    /** 报价后规格改变必须重新确认，不能静默提交新规格。 */
    @Test
    void changedSpecsInvalidateQuoteBeforeAnyOrderIsWritten() throws Exception {
        useCatalogSkuForReview();
        var token = SettlementRules.quoteToken(placement.quote(new SettlementQuoteQry(1L, List.of(sku), null)));
        skuProperties(List.of(new com.lemnt.mall.domain.product.model.ProductSkuSpec(null, "颜色", null, "新规格")));
        var failure = assertThrows(IllegalStateException.class, () -> placement.checkoutCart(
                new SettlementOrderSubmitCmd(1L, "CHANGED-SPEC", List.of(sku), null, token)));
        assertEquals("价格或数量已变化，请重新预览并确认", failure.getMessage());
        assertEquals(0, count("trade_order"));
    }


    /** 字典关联从旧未知变为已知，名称和值未变也必须重新确认报价。 */
    @Test
    void changingOnlySpecIdsRejectsOldQuoteAndCreatesNoOrder() throws Exception {
        useCatalogSkuForReview();
        skuProperties(List.of(new com.lemnt.mall.domain.product.model.ProductSkuSpec(null, "颜色", null, "黑色")));
        var token = SettlementRules.quoteToken(placement.quote(new SettlementQuoteQry(1L, List.of(sku), null)));
        var dict = mallComponents.get(com.lemnt.mall.application.product.service.ProductSpecService.class);
        var spec = dict.create(new com.lemnt.mall.application.product.cmd.ProductSpecCreateCmd("颜色", null));
        var option = dict.createValue(new com.lemnt.mall.application.product.cmd.ProductSpecValueCreateCmd(spec.id(), "黑色", null));
        skuProperties(List.of(new com.lemnt.mall.domain.product.model.ProductSkuSpec(spec.id(), "颜色", option.id(), "黑色")));
        assertThrows(IllegalStateException.class, () -> placement.checkoutCart(new SettlementOrderSubmitCmd(
                1L, "CHANGED-SPEC-ID", List.of(sku), null, token)));
        assertEquals(0, count("trade_order"));
    }

    private void skuProperties(List<com.lemnt.mall.domain.product.model.ProductSkuSpec> properties) {
        mallComponents.get(com.lemnt.mall.infrastructure.product.mapper.ProductSkuMapper.class).update(null,
                com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaUpdate(com.lemnt.mall.infrastructure.product.dataobject.ProductSkuDO.class)
                        .eq(com.lemnt.mall.infrastructure.product.dataobject.ProductSkuDO::getId, sku)
                        .set(com.lemnt.mall.infrastructure.product.dataobject.ProductSkuDO::getProperties,
                                com.alibaba.fastjson2.JSON.toJSONString(properties)));
    }
    @Test
    void commentsUseRealItemIdsAndOnlyAllItemsFinishTheOrderAndLogOnce() throws Exception {
        var order = completedReviewOrderWithTwoItems();
        assertEquals(2,order.items().size());assertFalse(order.commentStatus());
        assertTrue(order.items().stream().allMatch(line -> line.orderItemId()!=null && Boolean.FALSE.equals(line.commentStatus())));
        var comments=mallComponents.get(ProductCommentService.class);
        var a=order.items().get(0);var b=order.items().get(1);
        var first=new CommentCreateCmd(1L,order.orderNo(),a.productId(),null,"第一项",5,4,false,List.of(),a.orderItemId());
        var saved=comments.create(first);assertEquals(a.orderItemId(),saved.orderItemId());
        assertFalse(orderRpc.findByOrderNo(order.orderNo()).commentStatus());
        assertEquals(1,orderRpc.findByOrderNo(order.orderNo()).items().stream().filter(line->Boolean.TRUE.equals(line.commentStatus())).count());
        assertEquals(404,assertThrows(com.lemnt.common.core.exception.ServiceException.class,()->comments.create(
                new CommentCreateCmd(1L,order.orderNo(),a.productId(),5,"伪造行",null,null,false,List.of(),b.orderItemId()))).getCode());
        comments.create(new CommentCreateCmd(1L,order.orderNo(),b.productId(),5,"第二项",null,null,false,List.of(),b.orderItemId()));
        var finished=orderRpc.findByOrderNo(order.orderNo());assertTrue(finished.commentStatus());assertNotNull(finished.finishTime());
        comments.create(first);
        assertEquals(finished.finishTime(),orderRpc.findByOrderNo(order.orderNo()).finishTime());
        var logs=tradeComponents.get(com.lemnt.trade.domain.order.gateway.OrderLogGateway.class).byOrderId(order.id());
        assertEquals(1,logs.stream().filter(log->log.operateType()==33).count());
        // 数据库ID/后续评价状态不能破坏原订单购买事实的幂等比较。
        tradeComponents.get(com.lemnt.trade.application.order.service.OrderSubmissionService.class).requireSame(
                orderPersistenceService.findByOrderNo(order.orderNo()),new com.lemnt.trade.domain.order.model.TradeOrder(null,
                        order.orderNo(),order.memberId(),order.totalAmount(),"RESERVING",orderPersistenceService.findByOrderNo(order.orderNo()).items().stream().map(line->
                        new com.lemnt.trade.domain.order.model.TradeOrderItem(line.productId(),line.productCode(),line.productName(),line.unitPrice(),
                                line.quantity(),line.lineAmount(),line.payAmount(),line.deliveryPrice(),line.spuId(),line.spuName(),line.properties(),line.picUrl())).toList(),
                        null,order.usePoint(),order.givePoint(),orderPersistenceService.findByOrderNo(order.orderNo()).delivery()));
        var detail=tradeComponents.get(com.lemnt.trade.application.fulfillment.executor.OrderDetailQueryExe.class).execute(order.orderNo());
        assertTrue(detail.commentStatus());assertEquals(finished.finishTime(),detail.finishTime());
        assertEquals(a.orderItemId(),detail.items().get(0).orderItemId());
    }

    @Test
    void committedRemoteCommentWithLostResponseIsRepairedByOriginalRetry() throws Exception {
        useCatalogSkuForReview();var order=paidOrder(null);fulfillmentService.ship(shipment(order.orderNo()));fulfillmentService.receive(order.orderNo());
        var line=orderRpc.findByOrderNo(order.orderNo()).items().get(0);
        var cmd=new CommentCreateCmd(1L,order.orderNo(),line.productId(),null,"丢响应",5,4,true,List.of(),line.orderItemId());
        loseNextCommentResponse=true;
        assertThrows(IllegalStateException.class,()->mallComponents.get(ProductCommentService.class).create(cmd));
        var stored=mallComponents.get(ProductCommentGateway.class).findByOrderItem(line.orderItemId());assertNotNull(stored);
        assertFalse(orderRpc.findByOrderNo(order.orderNo()).items().get(0).commentStatus());
        var repaired=mallComponents.get(ProductCommentService.class).create(cmd);assertEquals(stored.id(),repaired.id());
        assertTrue(orderRpc.findByOrderNo(order.orderNo()).commentStatus());
        assertEquals(1,tradeComponents.get(com.lemnt.trade.domain.order.gateway.OrderLogGateway.class).byOrderId(order.id()).stream().filter(log->log.operateType()==33).count());
        assertEquals(1,count("mall_product_comment"));
    }

    @Test
    void localCommentStateFailureRollsBackFlagsAndLogButOriginalRetryKeepsRemoteId() throws Exception {
        useCatalogSkuForReview();var order=paidOrder(null);fulfillmentService.ship(shipment(order.orderNo()));fulfillmentService.receive(order.orderNo());
        var line=orderRpc.findByOrderNo(order.orderNo()).items().get(0);
        var cmd=new CommentCreateCmd(1L,order.orderNo(),line.productId(),5,"事务修复",null,null,false,List.of(),line.orderItemId());
        sql(tradeSource,"ALTER TABLE trade_order ADD CONSTRAINT reject_review_done CHECK(comment_status IS NULL OR comment_status=FALSE)");
        assertThrows(RuntimeException.class,()->mallComponents.get(ProductCommentService.class).create(cmd));
        var saved=mallComponents.get(ProductCommentGateway.class).findByOrderItem(line.orderItemId());assertNotNull(saved);
        assertFalse(orderRpc.findByOrderNo(order.orderNo()).commentStatus());
        assertFalse(orderRpc.findByOrderNo(order.orderNo()).items().get(0).commentStatus());
        assertEquals(0,tradeComponents.get(com.lemnt.trade.domain.order.gateway.OrderLogGateway.class).byOrderId(order.id()).stream().filter(log->log.operateType()==33).count());
        sql(tradeSource,"ALTER TABLE trade_order DROP CONSTRAINT reject_review_done");
        assertEquals(saved.id(),mallComponents.get(ProductCommentService.class).create(cmd).id());
        var finished=orderRpc.findByOrderNo(order.orderNo());assertTrue(finished.commentStatus());assertNotNull(finished.finishTime());
        assertEquals(1,count("mall_product_comment"));
    }

    private SettlementOrder completedReviewOrderWithTwoItems() {
        useCatalogSkuForReview();
        var firstSku = sku;
        var catalog = mallComponents.get(com.lemnt.mall.application.product.service.ProductCatalogService.class);
        var spuGateway = mallComponents.get(com.lemnt.mall.domain.product.gateway.ProductSpuGateway.class);
        var firstSpu = spuGateway.findSpu(productSkuGateway.catalogSku(firstSku).spuId());
        var second = catalog.createSpu(com.lemnt.mall.domain.product.model.ProductCatalogValidation.newSpu(
                new ProductSpu(null,"COMMENT-TWO","第二评价商品",firstSpu.categoryId(),firstSpu.brandId(),null,null,null,
                        List.of(new ProductSku(null,null,"COMMENT-SKU-TWO","第二规格",BigDecimal.TEN,10,null,Map.of(),null)))));
        spuGateway.shelf(second.id(),true);
        Long secondSku = second.skus().get(0).id();
        cartService.saveCart(1L, secondSku, 1, true);
        var ids = List.of(firstSku,secondSku);
        var quote = placement.quote(new SettlementQuoteQry(1L,ids,null));
        var created = placement.checkoutCart(new SettlementOrderSubmitCmd(1L,"REVIEW-TWO",ids,null,SettlementRules.quoteToken(quote)));
        worker().execute(50);
        var payment = paymentCreate.execute(created.orderNo(),"ALIPAY");
        notify.execute(new VerifiedPaymentNotification(payment.paymentNo(),created.orderNo(),"ALIPAY","CH-C",created.totalAmount(),"N-C"));
        fulfillmentService.ship(shipment(created.orderNo()));fulfillmentService.receive(created.orderNo());
        return orderRpc.findByOrderNo(created.orderNo());
    }

    @Test
    void concurrentDifferentItemReviewsConvergeAndCreateOneCompletionLog() throws Exception {
        var order=completedReviewOrderWithTwoItems();
        var service=mallComponents.get(ProductCommentService.class);
        var pool=java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            var requests=order.items().stream().map(line->pool.submit(()->service.create(new CommentCreateCmd(1L,
                    order.orderNo(),line.productId(),null,"并发评价",5,4,false,List.of(),line.orderItemId())))).toList();
            var ids=new java.util.HashSet<Long>();
            for(var request:requests)ids.add(request.get(10,java.util.concurrent.TimeUnit.SECONDS).id());
            assertEquals(2,ids.size());
        } finally { pool.shutdownNow(); }
        assertTrue(orderRpc.findByOrderNo(order.orderNo()).commentStatus());
        assertEquals(2,count("mall_product_comment"));
        assertEquals(1,tradeComponents.get(com.lemnt.trade.domain.order.gateway.OrderLogGateway.class).byOrderId(order.id()).stream().filter(log->log.operateType()==33).count());
    }

    @Test
    void historicalUnknownFlagsAreNotFakedFalseAndKnownLegacyReviewBindsRealItem() throws Exception {
        useCatalogSkuForReview();var order=paidOrder(null);fulfillmentService.ship(shipment(order.orderNo()));fulfillmentService.receive(order.orderNo());
        var actual=orderRpc.findByOrderNo(order.orderNo());var line=actual.items().get(0);
        sql(tradeSource,"UPDATE trade_order SET comment_status=NULL WHERE id="+order.id());
        sql(tradeSource,"UPDATE trade_order_item SET comment_status=NULL WHERE order_id="+order.id());
        assertNull(orderRpc.findByOrderNo(order.orderNo()).commentStatus());assertNull(orderRpc.findByOrderNo(order.orderNo()).items().get(0).commentStatus());
        var old=mallComponents.get(ProductCommentGateway.class).save(new com.lemnt.mall.domain.product.model.ProductComment(null,
                productSkuGateway.catalogSku(sku).spuId(),sku,order.orderNo(),1L,5,"旧购买评价",true,null,null,null,null));
        var repaired=mallComponents.get(ProductCommentService.class).create(new CommentCreateCmd(1L,order.orderNo(),sku,5,"旧购买评价"));
        assertEquals(old.id(),repaired.id());assertEquals(line.orderItemId(),repaired.orderItemId());assertEquals(order.id(),repaired.orderId());
        assertNull(repaired.spuName());assertNull(repaired.descriptionScores());assertNull(repaired.userNickname());
        assertTrue(orderRpc.findByOrderNo(order.orderNo()).commentStatus());
    }

    @Test
    void submittedItemIdsAndReviewFlagsCannotForgeDatabaseGeneratedMetadata() throws Exception {
        useCatalogSkuForReview();var parent=productSkuGateway.catalogSku(sku).spuId();
        var fakeLine = new com.lemnt.trade.domain.order.model.TradeOrderItem(sku, "COMMENT-SKU", "购买商品", BigDecimal.TEN, 1,
                BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, parent, "购买商品", List.of(), null, 999999L, true);
        var fake = new com.lemnt.trade.domain.order.model.TradeOrder(null, "META-FORGE", 1L, BigDecimal.TEN,
                "RESERVING", List.of(fakeLine), null, 0L, 0L, null, true, java.time.LocalDateTime.of(2000, 1, 1, 0, 0));
        // 直接验证交易持久化边界，避免绕过结算回执校验构造无效 RPC 下单请求。
        tradeComponents.get(com.lemnt.trade.application.order.service.OrderSubmissionService.class).create(fake);
        var saved = orderRpc.findByOrderNo(fake.orderNo());
        assertNotEquals(999999L,saved.items().get(0).orderItemId());assertNotNull(saved.items().get(0).orderItemId());
        assertFalse(saved.items().get(0).commentStatus());assertFalse(saved.commentStatus());assertNull(saved.finishTime());
    }

    @Test
    void systemCommentRequiresExpiredReceiptAndKeepsOneCompletionLog() {
        var order = completedReviewOrderWithTwoItems();
        var service = tradeComponents.get(com.lemnt.trade.application.order.service.OrderCommentService.class);
        var time = tradeOrderGateway.databaseNow();
        assertFalse(service.createSystem(order.orderNo(), time.minusDays(7)));
        assertTrue(service.createSystem(order.orderNo(), time.plusDays(1)));
        assertFalse(service.createSystem(order.orderNo(), time.plusDays(1)));
        var saved = orderRpc.findByOrderNo(order.orderNo());
        assertTrue(saved.commentStatus());
        var finish = saved.finishTime();
        assertEquals(1, tradeComponents.get(com.lemnt.trade.domain.order.gateway.OrderLogGateway.class)
                .byOrderId(order.id()).stream().filter(v -> v.operateType() == 34).count());
        for (var line : saved.items()) {
            var comment = mallComponents.get(com.lemnt.mall.domain.product.gateway.ProductCommentGateway.class).findByOrderItem(line.orderItemId());
            assertEquals("", comment.content()); assertEquals(5, comment.score()); assertFalse(comment.anonymous());
        }
        assertEquals(finish, orderRpc.findByOrderNo(order.orderNo()).finishTime());
    }

    @Test
    void systemOriginalRetryRepairsRemoteCommitAfterLostResponse() {
        var order = completedReviewOrderWithTwoItems();
        var service = tradeComponents.get(com.lemnt.trade.application.order.service.OrderCommentService.class);
        var cutoff = tradeOrderGateway.databaseNow().plusDays(1);
        loseNextCommentResponse = true;
        assertThrows(IllegalStateException.class, () -> service.createSystem(order.orderNo(), cutoff));
        assertFalse(orderRpc.findByOrderNo(order.orderNo()).commentStatus());
        var first = order.items().get(0).orderItemId();
        var comments = mallComponents.get(com.lemnt.mall.domain.product.gateway.ProductCommentGateway.class);
        var remoteId = comments.findByOrderItem(first).id();
        assertTrue(service.createSystem(order.orderNo(), cutoff));
        assertEquals(remoteId, comments.findByOrderItem(first).id());
    }

}
