package com.lemnt.order.start.checkout;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.client.dto.MemberAddressDTO;
import com.lemnt.member.client.dto.MemberPointConfigDTO;
import com.lemnt.order.application.aftersale.cmd.AfterSaleCreateCmd;
import com.lemnt.order.application.aftersale.cmd.AfterSaleRefuseCmd;
import com.lemnt.order.application.aftersale.cmd.AfterSaleRejectCmd;
import com.lemnt.order.application.aftersale.cmd.AfterSaleReturnCmd;
import com.lemnt.order.application.aftersale.executor.AfterSalePageQueryExe;
import com.lemnt.order.application.aftersale.qry.AfterSalePageQry;
import com.lemnt.order.application.aftersale.service.AfterSaleService;
import com.lemnt.order.application.delivery.cmd.DeliveryChargeCmd;
import com.lemnt.order.application.delivery.cmd.DeliveryExpressTemplateCreateCmd;
import com.lemnt.order.application.delivery.service.DeliveryManagementService;
import com.lemnt.order.application.dubbo.TradeOrderClientImpl;
import com.lemnt.order.application.fulfillment.cmd.RefundRejectCmd;
import com.lemnt.order.application.fulfillment.cmd.RefundRequestCmd;
import com.lemnt.order.application.fulfillment.cmd.ShipmentCreateCmd;
import com.lemnt.order.application.fulfillment.executor.OrderDetailQueryExe;
import com.lemnt.order.application.fulfillment.executor.OrderPageQueryExe;
import com.lemnt.order.application.fulfillment.executor.ShipmentDetailQueryExe;
import com.lemnt.order.application.fulfillment.executor.ShipmentPageQueryExe;
import com.lemnt.order.application.fulfillment.qry.OrderPageQry;
import com.lemnt.order.application.fulfillment.qry.ShipmentPageQry;
import com.lemnt.order.application.fulfillment.service.OrderFulfillmentService;
import com.lemnt.order.application.member.service.MemberValidationService;
import com.lemnt.order.application.order.cmd.OrderAdjustPriceCmd;
import com.lemnt.order.application.order.cmd.OrderRemarkUpdateCmd;
import com.lemnt.order.application.order.executor.OrderRemarkUpdateCmdExe;
import com.lemnt.order.application.order.executor.OrderStatisticsQueryExe;
import com.lemnt.order.application.order.executor.TradeOrderCloseCmdExe;
import com.lemnt.order.application.order.service.OrderAdjustmentService;
import com.lemnt.order.application.order.cmd.OrderCommentCreateCmd;
import com.lemnt.order.application.order.service.OrderCommentService;
import com.lemnt.order.application.order.service.OrderLifecycleService;
import com.lemnt.order.application.order.service.OrderLogService;
import com.lemnt.order.application.order.service.OrderPersistenceService;
import com.lemnt.order.application.order.service.OrderRemoteService;
import com.lemnt.order.application.order.service.OrderRollbackPublisher;
import com.lemnt.order.application.order.service.OrderSubmissionService;
import com.lemnt.order.application.paymentrefund.executor.PaymentRefundCreateCmdExe;
import com.lemnt.order.application.settlement.cmd.SettlementDirectCmd;
import com.lemnt.order.application.settlement.cmd.SettlementOrderSubmitCmd;
import com.lemnt.order.application.settlement.executor.SettlementReceiptQueryExe;
import com.lemnt.order.application.settlement.qry.SettlementQuoteQry;
import com.lemnt.order.application.settlement.service.OrderPlacementService;
import com.lemnt.order.application.settlement.service.SettlementDeliveryService;
import com.lemnt.order.application.settlement.service.SettlementOrderService;
import com.lemnt.order.application.settlement.service.SettlementPreparationService;
import com.lemnt.order.application.settlement.service.SettlementRecordService;
import com.lemnt.order.application.settlement.service.SettlementService;
import com.lemnt.order.client.TradeOrderClient;
import com.lemnt.order.client.bo.OrderCreateBo;
import com.lemnt.order.client.dto.OrderDTO;
import com.lemnt.order.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.order.domain.aftersale.gateway.AfterSaleLogGateway;
import com.lemnt.order.domain.aftersale.model.AfterSale;
import com.lemnt.order.domain.aftersale.model.AfterSaleLog;
import com.lemnt.order.domain.cart.gateway.CartSnapshotGateway;
import com.lemnt.order.domain.config.gateway.TradeConfigGateway;
import com.lemnt.order.domain.delivery.gateway.DeliveryExpressGateway;
import com.lemnt.order.domain.delivery.gateway.PickUpStoreGateway;
import com.lemnt.order.domain.delivery.model.DeliveryExpress;
import com.lemnt.order.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.order.domain.fulfillment.gateway.ShipmentGateway;
import com.lemnt.order.domain.fulfillment.model.RefundRequest;
import com.lemnt.order.domain.inventory.gateway.InventoryGateway;
import com.lemnt.order.domain.member.model.MemberSnapshot;
import com.lemnt.order.domain.order.gateway.OrderBenefitsGateway;
import com.lemnt.order.domain.order.gateway.OrderLogGateway;
import com.lemnt.order.domain.order.gateway.OrderRollbackGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.order.domain.order.model.OrderLog;
import com.lemnt.order.domain.order.model.TradeOperator;
import com.lemnt.order.domain.order.model.TradeOrder;
import com.lemnt.order.domain.order.model.TradeOrderItem;
import com.lemnt.order.domain.product.gateway.ProductCatalogGateway;
import com.lemnt.order.domain.product.model.ProductSnapshot;
import com.lemnt.order.domain.promotion.gateway.PromotionPricingGateway;
import com.lemnt.order.domain.settlement.gateway.SettlementGateway;
import com.lemnt.order.domain.settlement.model.Settlement;
import com.lemnt.order.domain.settlement.model.SettlementOrder;
import com.lemnt.order.domain.settlement.model.SettlementRules;
import com.lemnt.order.infrastructure.delivery.dataobject.DeliveryExpressTemplateChargeDO;
import com.lemnt.order.infrastructure.delivery.mapper.DeliveryExpressTemplateChargeMapper;
import com.lemnt.order.infrastructure.order.mapper.TradeOrderMapper;
import com.lemnt.order.infrastructure.product.gateway.ProductCatalogGatewayImpl;
import com.lemnt.order.start.TradeOrderScheduler;
import com.lemnt.order.start.support.CommerceEventPump;
import com.lemnt.order.start.support.CommerceTestDefaults;
import com.lemnt.order.start.support.CommerceTestFixture;
import com.lemnt.pay.application.payment.executor.MemberPaymentStatusQueryExe;
import com.lemnt.pay.application.payment.executor.PaymentNotifyCmdExe;
import com.lemnt.pay.application.payment.executor.PaymentPageQueryExe;
import com.lemnt.pay.application.payment.executor.TradePaymentCreateCmdExe;
import com.lemnt.pay.application.payment.qry.MemberPaymentStatusQry;
import com.lemnt.pay.application.payment.qry.PaymentPageQry;
import com.lemnt.pay.application.paymentrefund.executor.PaymentRefundPageQueryExe;
import com.lemnt.pay.application.paymentrefund.qry.PaymentRefundPageQry;
import com.lemnt.pay.domain.channel.gateway.PaymentChannelCloseGateway;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.channel.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundChannelGateway;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;
import com.lemnt.pay.infrastructure.channel.support.PayChannelConfigResolver;
import com.lemnt.pay.infrastructure.channel.factory.PaymentChannelFactory;
import com.lemnt.pay.infrastructure.channel.adapter.AbstractChannelAdapter;
import com.lemnt.pay.infrastructure.paymentrefund.gateway.PaymentRefundChannelGatewayImpl;
import com.lemnt.product.application.product.cmd.ProductCommentCreateCmd;
import com.lemnt.product.application.product.cmd.ProductCommentReplyCmd;
import com.lemnt.product.application.product.cmd.ProductCommentVisibleCmd;
import com.lemnt.product.application.product.cmd.ProductSpecCreateCmd;
import com.lemnt.product.application.product.cmd.ProductSpecUpdateCmd;
import com.lemnt.product.application.product.cmd.ProductSpecValueCreateCmd;
import com.lemnt.product.application.product.cmd.ProductSpecValueUpdateCmd;
import com.lemnt.product.application.product.executor.ProductCommentPageQueryExe;
import com.lemnt.product.application.product.executor.ProductCommentVisibleCmdExe;
import com.lemnt.product.application.product.qry.ProductCommentPageQry;
import com.lemnt.product.application.product.service.ProductCatalogService;
import com.lemnt.product.application.product.service.ProductCommentService;
import com.lemnt.product.application.product.service.ProductInventoryService;
import com.lemnt.product.application.product.service.ProductManagementService;
import com.lemnt.product.application.product.service.ProductSpecService;
import com.lemnt.product.client.ProductCommentClient;
import com.lemnt.product.domain.product.gateway.ProductBrandGateway;
import com.lemnt.product.domain.product.gateway.ProductCategoryGateway;
import com.lemnt.product.domain.product.gateway.ProductCommentGateway;
import com.lemnt.product.domain.product.gateway.ProductSkuGateway;
import com.lemnt.product.domain.product.gateway.ProductSpuGateway;
import com.lemnt.product.domain.product.model.ProductBrand;
import com.lemnt.product.domain.product.model.ProductCategory;
import com.lemnt.product.domain.product.model.Product;
import com.lemnt.product.domain.product.model.ProductCatalogValidation;
import com.lemnt.product.domain.product.model.ProductComment;
import com.lemnt.product.domain.product.model.ProductSku;
import com.lemnt.product.domain.product.model.ProductSkuSpec;
import com.lemnt.product.domain.product.model.ProductSpu;
import com.lemnt.product.infrastructure.product.dataobject.ProductSkuDO;
import com.lemnt.product.infrastructure.product.mapper.ProductSkuMapper;
import com.lemnt.promotion.application.cart.cmd.CartSaveCmd;
import com.lemnt.promotion.application.cart.executor.CartSaveCmdExe;
import com.lemnt.promotion.application.cart.service.CartService;
import com.lemnt.promotion.application.coupon.cmd.CouponClaimCmd;
import com.lemnt.promotion.application.coupon.cmd.CouponPublicClaimCmd;
import com.lemnt.promotion.application.coupon.qry.CouponPageQry;
import com.lemnt.promotion.application.coupon.service.CouponIssuanceService;
import com.lemnt.promotion.application.coupon.service.CouponService;
import com.lemnt.promotion.domain.cart.gateway.CartGateway;
import com.lemnt.promotion.domain.coupon.gateway.CouponGateway;
import com.lemnt.promotion.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.promotion.domain.coupon.model.CouponRules;
import com.lemnt.promotion.domain.coupon.model.CouponTemplate;
import com.lemnt.promotion.domain.member.gateway.MemberLockGateway;

import com.alibaba.fastjson2.JSON;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.RedissonLockExecutor;
import com.baomidou.lock.spring.boot.autoconfigure.Lock4jProperties;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.Instant;
import java.time.LocalDateTime;
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
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Two isolated databases, actual production gateways/mappers and transaction proxies. RPC is a test
 * bridge; this is not a Nacos/Dubbo runtime or real payment-channel test.
 */
class CheckoutFlowIntegrationTest {
    boolean loseNextCommentResponse;

    CommerceTestFixture tradeComponents, mallComponents;
    CommerceEventPump events;
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
    SettlementGateway settlementOrderGateway;
    SettlementGateway settlementAbandonGateway;
    Map<String, Settlement> settlementCache = new ConcurrentHashMap<>();
    OrderFulfillmentService fulfillmentService;
    RollbackMessages rollbackMessages = new RollbackMessages();
    OrderRemoteService remotes;

    class RollbackMessages implements OrderRollbackGateway {
        public void publish(String no) {
            tradeComponents.get(OrderRollbackPublisher.class).publish(no);
        }

        int execute(int limit) {
            return events.drain();
        }
    }

    AbstractChannelAdapter refundClient;
    TradePaymentGateway payments;
    OrderPlacementService placement;
    OrderLifecycleService lifecycle;
    TradeOrderCloseCmdExe closer;
    TradePaymentCreateCmdExe paymentCreate;
    PaymentNotifyCmdExe notify;

    interface CatalogBridge extends ProductCatalogGateway, InventoryGateway {
    }

    CatalogBridge bridge;
    SettlementOrderService orderRpc;
    OrderBenefitsGateway benefits;
    boolean deliveryPointGift;
    Long sku;
    MemberAddressDTO deliveryAddress =
        new MemberAddressDTO(7L, 1L, "收件人", "13800000000", 110101L, "原始地址");
    boolean failNextRefundBenefits;
    boolean loseNextOrderResponse;
    boolean rejectNextReserve;
    boolean loseNextReserveResponse;
    boolean loseNextConfirmResponse;
    Map<String, String> quoteTokens = new HashMap<>();

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

    /**
     * 用本地重入锁替代 Redis 传输，保留真实锁模板及事务完成回调。
     */
    private LockTemplate memberLocks() throws Exception {
        var locks = new ConcurrentHashMap<String, ReentrantLock>();
        var executor = Mockito.mock(RedissonLockExecutor.class);
        Mockito.when(executor.renewal()).thenReturn(true);
        Mockito.when(
                executor.acquire(
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyLong(),
                    ArgumentMatchers.anyLong()))
            .thenAnswer(
                invocation -> {
                    var lock =
                        locks.computeIfAbsent(
                            invocation.getArgument(0), key -> new ReentrantLock());
                    if (!lock.tryLock(3, TimeUnit.SECONDS)) {
                        return null;
                    }
                    var instance = Mockito.mock(RLock.class);
                    return instance;
                });
        // LockTemplate 调用执行器解锁；测试替身显式释放对应本地锁。
        Mockito.when(
                executor.releaseLock(
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.any()))
            .thenAnswer(
                invocation -> {
                    var lock = locks.get(invocation.<String>getArgument(0));
                    if (lock == null || !lock.isHeldByCurrentThread()) {
                        return false;
                    }
                    lock.unlock();
                    return true;
                });
        var properties = new Lock4jProperties();
        properties.setPrimaryExecutor(RedissonLockExecutor.class);
        var template = new LockTemplate();
        template.setProperties(properties);
        template.setExecutors(List.of(executor));
        template.afterPropertiesSet();
        return template;
    }

    @BeforeEach
    void setup() throws Exception {
        tradeComponents = new CommerceTestFixture();
        mallComponents = tradeComponents;
        events = new CommerceEventPump(tradeComponents);
        events.install();
        CommerceTestDefaults.install(
            tradeComponents,
            events,
            () -> 0L,
            () -> new MemberPointConfigDTO(deliveryPointGift, 1, 0, 2),
            () -> deliveryAddress);
        tradeComponents.register(LockTemplate.class, memberLocks());
        tradeSource = tradeComponents.source("order");
        mallSource = tradeComponents.source("promotion");
        tradeSchema = tradeComponents.schema("order");
        mallSchema = tradeComponents.schema("promotion");
        events.before =
            e -> {
                if ("AFTER_SALE_COMPLETED".equals(e.eventType()) && failNextRefundBenefits) {
                    failNextRefundBenefits = false;
                    throw new IllegalStateException("模拟权益消费者不可用");
                }
            };
        events.afterProduct =
            e -> {
                if (loseNextConfirmResponse) {
                    loseNextConfirmResponse = false;
                    throw new IllegalStateException("库存消费已提交但确认丢失");
                }
            };
        tradeComponents.intercept(
            (type, method, args, call) -> {
                var result = call.call();
                if (type == ProductCommentClient.class
                    && (method.getName().equals("saveVerified")
                    || method.getName().equals("saveSystem"))
                    && loseNextCommentResponse) {
                    loseNextCommentResponse = false;
                    throw new IllegalStateException("评价已提交但响应丢失");
                }
                return result;
            });
        tradeComponents
            .get(DeliveryExpressGateway.class)
            .insert(new DeliveryExpress(null, "SF", "顺丰", null, 0, 0));
        refundClient = Mockito.mock(AbstractChannelAdapter.class);
        Mockito.when(refundClient.channel()).thenReturn("ALIPAY");
        var payChannelConfigs = Mockito.mock(PayChannelConfigResolver.class);
        Mockito.when(payChannelConfigs.resolve(Mockito.any(), Mockito.any()))
            .thenReturn(
                new PayChannelConfig(
                    1L, 1L, "ALIPAY", 0, BigDecimal.ZERO, null, "{}", null));
        tradeComponents.register(
            PaymentRefundChannelGateway.class,
            new PaymentRefundChannelGatewayImpl(
                new PaymentChannelFactory(List.of(refundClient)),
                payChannelConfigs));
        productSkuGateway = tradeComponents.get(ProductSkuGateway.class);
        productInventoryService = tradeComponents.get(ProductInventoryService.class);
        checkoutAccountGateway = tradeComponents.get(MemberLockGateway.class);
        cartGateway = tradeComponents.get(CartGateway.class);
        couponTemplateGateway = tradeComponents.get(CouponTemplateGateway.class);
        couponGateway = tradeComponents.get(CouponGateway.class);
        cartService = tradeComponents.get(CartService.class);
        couponIssuanceService = tradeComponents.get(CouponIssuanceService.class);
        inventoryConfirmationGateway = tradeComponents.get(InventoryConfirmationGateway.class);
        shipmentGateway = tradeComponents.get(ShipmentGateway.class);
        refundRequestGateway = tradeComponents.get(AfterSaleGateway.class);
        tradeOrderGateway = tradeComponents.get(TradeOrderGateway.class);
        orderPersistenceService = tradeComponents.get(OrderPersistenceService.class);
        cacheStore = new MemorySettlementGateway(settlementCache);
        settlements = cacheStore;
        settlementOrderGateway = cacheStore;
        settlementAbandonGateway = cacheStore;
        tradeComponents.register(SettlementGateway.class, cacheStore);
        checkoutService = tradeComponents.get(SettlementRecordService.class);
        sku =
            productSkuGateway
                .save(
                    Product.create(
                        "SKU1", "sku", null, null, new BigDecimal("10.00"), 10))
                .id();
        productSkuGateway.updateStatus(sku, "ON_SHELF");
        var catalog = tradeComponents.get(ProductCatalogGatewayImpl.class);
        bridge =
            new CatalogBridge() {
                public ProductSnapshot findOnShelf(Long id) {
                    return catalog.findOnShelf(id);
                }

                public Map<Long, ProductSnapshot> findOnShelfBatch(List<Long> ids) {
                    return catalog.findOnShelfBatch(ids);
                }

                public boolean reserveStock(String no, Long id, Integer quantity) {
                    if (rejectNextReserve) {
                        rejectNextReserve = false;
                        return false;
                    }
                    boolean result =
                        tradeComponents.call(
                            () ->
                                productInventoryService.reserveStock(
                                    no, id, quantity));
                    if (loseNextReserveResponse) {
                        loseNextReserveResponse = false;
                        throw new IllegalStateException("库存已提交但响应丢失");
                    }
                    return result;
                }

                public void confirmStock(String no, Long id) {
                    tradeComponents.call(
                        () -> {
                            productInventoryService.confirmStock(no, id);
                            return null;
                        });
                }
            };
        tradeComponents.register(ProductCatalogGateway.class, bridge);
        tradeComponents.register(InventoryGateway.class, bridge);
        benefits = tradeComponents.get(OrderBenefitsGateway.class);
        lifecycle = tradeComponents.get(OrderLifecycleService.class);
        remotes = tradeComponents.get(OrderRemoteService.class);
        var actual = tradeComponents.get(TradeOrderClientImpl.class);
        var client =
            new TradeOrderClient() {
                public OrderDTO create(OrderCreateBo input) {
                    var saved = orderProvider().create(input);
                    if (loseNextOrderResponse) {
                        loseNextOrderResponse = false;
                        throw new IllegalStateException("订单已提交但响应丢失");
                    }
                    return saved;
                }

                public OrderDTO createPriced(OrderCreateBo input) {
                    var saved = orderProvider().createPriced(input);
                    if (loseNextOrderResponse) {
                        loseNextOrderResponse = false;
                        throw new IllegalStateException("订单已提交但响应丢失");
                    }
                    return saved;
                }

                public OrderDTO getByOrderNo(String no) {
                    return actual.getByOrderNo(no);
                }
            };
        tradeComponents.register(TradeOrderClient.class, client);
        orderRpc = new SettlementOrderService(client);
        tradeComponents.register(SettlementOrderService.class, orderRpc);
        coupons = tradeComponents.get(CouponService.class);
        cartAdditions = tradeComponents.get(CartSaveCmdExe.class);
        placement = tradeComponents.get(OrderPlacementService.class);
        fulfillmentService = tradeComponents.get(OrderFulfillmentService.class);
        closer = tradeComponents.get(TradeOrderCloseCmdExe.class);
        paymentCreate = tradeComponents.get(TradePaymentCreateCmdExe.class);
        payments = tradeComponents.get(TradePaymentGateway.class);
        notify = tradeComponents.get(PaymentNotifyCmdExe.class);
        cartService.saveCart(1L, sku, 2, true);
    }

    TradeOrderClientImpl orderProvider() {
        return new TradeOrderClientImpl(
            tradeComponents.get(OrderSubmissionService.class),
            orderPersistenceService,
            remotes,
            tradeOrderGateway,
            tradeComponents.get(TradeConfigGateway.class),
            tradeComponents.get(PickUpStoreGateway.class));
    }

    @AfterEach
    void close() throws Exception {
        if (tradeComponents != null) tradeComponents.close();
    }

    RollbackMessages worker() {
        return rollbackMessages;
    }

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
        return placement.checkoutCart(
            new SettlementOrderSubmitCmd(1L, key, List.of(sku), coupon, token));
    }

    JdbcDataSource sqlSource(String sql, JdbcDataSource fallback) {
        for (String owner : List.of("product", "promotion", "pay", "order"))
            if (Pattern.compile("(?<![A-Za-z0-9_])" + owner + "_").matcher(sql).find())
                return tradeComponents.source(owner);
        return fallback;
    }

    void sql(JdbcDataSource source, String sql) throws Exception {
        try (var c = sqlSource(sql, source).getConnection();
             var statement = c.createStatement()) {
            statement.execute(sql);
        }
    }

    long count(String table) throws Exception {
        return new JdbcTemplate(sqlSource(table, tradeSource))
            .queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
    }

    /**
     * 先调价后创建支付使用新金额；已有预支付单则在持久化前拒绝调价。
     */
    @Test
    void repricingBeforePaymentIsConsistentAndExistingPaymentPreventsFurtherChanges()
        throws Exception {
        var order = place("ADJUST-PAYMENT", null);
        var adjustments = tradeComponents.get(OrderAdjustmentService.class);
        adjustments.adjustPrice(
            new OrderAdjustPriceCmd(order.orderNo(), new BigDecimal("8.00"), 9L));
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        assertEquals(new BigDecimal("8.00"), payment.amount());
        assertThrows(
            ServiceException.class,
            () ->
                adjustments.adjustPrice(
                    new OrderAdjustPriceCmd(
                        order.orderNo(), new BigDecimal("7.00"), 9L)));
        assertEquals(
            new BigDecimal("8.00"),
            tradeComponents
                .get(OrderPersistenceService.class)
                .findByOrderNo(order.orderNo())
                .totalAmount());
        assertEquals(
            new BigDecimal("8.00"), payments.findByPaymentNo(payment.paymentNo()).amount());
        notify.execute(
            new VerifiedPaymentNotification(
                payment.paymentNo(),
                order.orderNo(),
                "ALIPAY",
                "ADJUST-PAID",
                payment.amount(),
                "ADJUST-NOTICE"));
        assertEquals(
            "PAID",
            tradeComponents
                .get(OrderPersistenceService.class)
                .findByOrderNo(order.orderNo())
                .status());
    }

    @Test
    void successfulCheckoutUsesServerPricesLocksCouponAndConsumesCartAfterReservation()
        throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, new BigDecimal("3.00")).id());
        var order = place("SYNC", coupon.id());
        assertEquals("PENDING_PAYMENT", order.status());
        assertEquals(new BigDecimal("17.00"), order.totalAmount());
        assertEquals(8, productSkuGateway.findById(sku).stock());
        assertEquals("LOCKED", couponGateway.coupon(1L, coupon.id(), false).status());
        assertTrue(cartGateway.cart(1L).isEmpty());
        assertTrue(
            events.pendingEvents().stream()
                .noneMatch(e -> "ORDER_CLOSED".equals(e.eventType())));
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        var notice =
            new VerifiedPaymentNotification(
                payment.paymentNo(),
                order.orderNo(),
                "ALIPAY",
                "SYNC-PAY",
                order.totalAmount(),
                "NOTICE-SYNC");
        notify.execute(notice);
        notify.execute(notice);
        assertEquals("USED", couponGateway.coupon(1L, coupon.id(), false).status());
        assertTrue(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
        assertEquals(8, productSkuGateway.findById(sku).stock());
    }

    /**
     * 库存与券分别在所属服务补偿；核券失败保留原消息，重试不会重复归还库存。
     */
    @Test
    void independentStockAndCouponRollbackRecoversWithIdempotentMqRetry() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, BigDecimal.ONE).id());
        var order = place("ROLLBACK-ATOMIC", coupon.id());
        assertTrue(closer.execute(order.orderNo()));
        sql(
            mallSource,
            "ALTER TABLE promotion_coupon ADD CONSTRAINT block_release CHECK(status<>'AVAILABLE')");
        assertEquals(0, worker().execute(50));
        assertEquals(10, productSkuGateway.findById(sku).stock());
        assertEquals("LOCKED", couponGateway.coupon(1L, coupon.id(), false).status());
        sql(mallSource, "ALTER TABLE promotion_coupon DROP CONSTRAINT block_release");
        assertEquals(1, worker().execute(50));
        assertEquals(10, productSkuGateway.findById(sku).stock());
        assertEquals("AVAILABLE", couponGateway.coupon(1L, coupon.id(), false).status());
        var eventNo =
            tradeComponents
                .jdbc("order")
                .queryForObject(
                    "SELECT event_no FROM order_message_outbox WHERE event_type='ORDER_CLOSED' AND business_key=?",
                    String.class,
                    order.orderNo());
        events.replay(eventNo);
        assertEquals(1, worker().execute(50));
        assertEquals(10, productSkuGateway.findById(sku).stock());
        assertEquals("AVAILABLE", couponGateway.coupon(1L, coupon.id(), false).status());
    }

    /**
     * 同步核券失败不继续扣库存，修复后原请求可以重试。
     */
    @Test
    void synchronousCouponCheckMustSucceedBeforeInventoryReservation() throws Exception {
        var original = remotes;
        var rejected =
            Mockito.mock(OrderBenefitsGateway.class, AdditionalAnswers.delegatesTo(benefits));
        Mockito.doThrow(new IllegalStateException("同步核券失败"))
            .when(rejected)
            .verify(Mockito.anyString());
        tradeComponents.register(OrderBenefitsGateway.class, benefits);
        remotes =
            new OrderRemoteService(
                orderPersistenceService,
                bridge,
                rejected,
                lifecycle,
                tradeComponents.bean(
                    "com.lemnt.order.domain.order.gateway.OrderBenefitsProgressGateway"),
                tradeComponents.bean(
                    "com.lemnt.order.domain.order.gateway.PromotionResourceGateway"),
                tradeComponents.bean(
                    "com.lemnt.order.application.order.service.OrderPaidCompletionService"));
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
        sql(mallSource, "UPDATE product_sku SET price=99.00 WHERE id=" + sku);
        assertEquals(order.orderNo(), place("REQ1", null).orderNo());
        assertEquals(
            new BigDecimal("20.00"), checkoutService.receipt(order.orderNo()).payableAmount());
        assertEquals(1, count("order_info"));
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
                    assertThrows(
                        ServiceException.class,
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
        assertEquals(1, count("order_info"));
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
        assertEquals(0, count("order_info"));
    }

    @Test
    void couponInvariantFailureRetainsPaidOrderAndRetriesMallCallback() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, BigDecimal.ONE).id());
        var order = place("COUPON-INVARIANT", coupon.id());
        worker().execute(50);
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        sql(
            mallSource,
            "UPDATE promotion_coupon SET status='AVAILABLE',order_no=NULL WHERE id="
                + coupon.id());
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
            "UPDATE order_info SET create_time=TIMESTAMP '2001-01-01 00:00:00' WHERE"
                + " order_no='"
                + order.orderNo()
                + "'");
        for (int i = 0; i < 50; i++) {
            sql(
                tradeSource,
                "INSERT INTO"
                    + " order_info(order_no,member_id,total_amount,status,create_time)"
                    + " VALUES('BLOCK"
                    + i
                    + "',1,1,'PENDING_PAYMENT',TIMESTAMP '2000-01-01 00:00:00')");
            sql(
                tradeSource,
                "INSERT INTO pay_payment(payment_no,order_no,channel,amount,status)"
                    + " VALUES('PAYBLOCK"
                    + i
                    + "','BLOCK"
                    + i
                    + "','ALIPAY',1,'WAITING')");
        }
        tradeComponents.register(
            PaymentChannelCloseGateway.class,
            payment -> !payment.orderNo().startsWith("BLOCK"));
        new TradeOrderScheduler(closer, tradeOrderGateway).closeExpired();
        assertEquals("CLOSED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals("CLOSED", orderPersistenceService.findByOrderNo("BLOCK0").status());
        events.drain();
        assertEquals("WAITING", payments.findByPaymentNo("PAYBLOCK0").status());
        assertTrue(
            events.pendingEvents().stream()
                .anyMatch(
                    e ->
                        "ORDER_CLOSED".equals(e.eventType())
                            && "BLOCK0".equals(e.aggregateNo())));
        assertEquals(10, productSkuGateway.findById(sku).stock());
    }

    @Test
    void changedPriceAfterPreviewRequiresExplicitReconfirmation() throws Exception {
        String token =
            SettlementRules.quoteToken(
                placement.quote(new SettlementQuoteQry(1L, List.of(sku), null)));
        sql(mallSource, "UPDATE product_sku SET price=11.00 WHERE id=" + sku);
        assertThrows(
            IllegalStateException.class,
            () ->
                placement.checkoutCart(
                    new SettlementOrderSubmitCmd(
                        1L, "STALE", List.of(sku), null, token)));
        assertEquals(0, count("order_info"));
        assertEquals(10, productSkuGateway.findById(sku).stock());
    }

    @Test
    void unavailableCartItemCanBeDeselectedButNotNewlySelected() {
        productSkuGateway.updateStatus(sku, "OFF_SHELF");
        var saver = tradeComponents.get(CartSaveCmdExe.class);
        assertFalse(saver.execute(new CartSaveCmd(1L, sku, 2, false)).selected());
        assertThrows(
            IllegalArgumentException.class,
            () -> saver.execute(new CartSaveCmd(1L, sku, 2, true)));
    }

    @Test
    void expiredCouponCannotBecomeLockedOrCreateOrder() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, BigDecimal.ONE).id());
        sql(
            mallSource,
            "UPDATE promotion_coupon SET valid_from=TIMESTAMP '2000-01-01"
                + " 00:00:00',valid_until=TIMESTAMP '2001-01-01 00:00:00' WHERE id="
                + coupon.id());
        assertThrows(ServiceException.class, () -> place("EXPIRED", coupon.id()));
        assertEquals(0, count("order_info"));
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
            assertEquals(1, count("order_info"));

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
            "UPDATE pay_payment SET amount=1.00 WHERE payment_no='"
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
        assertThrows(IllegalArgumentException.class, () -> notify.execute(notice));
        assertEquals("WAITING", payments.findByPaymentNo(payment.paymentNo()).status());
        assertEquals(
            "PENDING_PAYMENT", orderPersistenceService.findByOrderNo(order.orderNo()).status());
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
                                        new SettlementDirectCmd.OrderLine(
                                            sku, 2)),
                                    "A",
                                    coupon.id()));
                            return true;
                        } catch (IllegalArgumentException
                                 | IllegalStateException
                                 | ServiceException ex) {
                            return false;
                        }
                    },
                    () -> {
                        try {
                            placement.createDirect(
                                new SettlementDirectCmd(
                                    1L,
                                    List.of(
                                        new SettlementDirectCmd.OrderLine(
                                            sku, 2)),
                                    "B",
                                    coupon.id()));
                            return true;
                        } catch (IllegalArgumentException
                                 | IllegalStateException
                                 | ServiceException ex) {
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
            // 失败可能发生在报价前或资源冻结时；后者保留原订单供关单恢复。
            long orderCount = count("order_info");
            assertTrue(orderCount >= 1 && orderCount <= 2);
            assertEquals(
                1,
                tradeComponents
                    .jdbc("order")
                    .queryForObject(
                        "SELECT COUNT(*) FROM order_info WHERE status='PENDING_PAYMENT'",
                        Long.class));
            var winner = couponGateway.coupon(1L, coupon.id(), false).orderNo();
            assertEquals("LOCKED", couponGateway.coupon(1L, coupon.id(), false).status());
            var losers =
                tradeComponents
                    .jdbc("order")
                    .queryForList(
                        "SELECT order_no FROM order_info WHERE order_no<>?",
                        String.class,
                        winner);
            for (var loser : losers) assertTrue(closer.execute(loser));
            events.drainOrThrow();
            assertEquals(winner, couponGateway.coupon(1L, coupon.id(), false).orderNo());
            assertEquals("LOCKED", couponGateway.coupon(1L, coupon.id(), false).status());
            assertEquals(8, productSkuGateway.findById(sku).stock());
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
        var management = tradeComponents.get(DeliveryManagementService.class);
        Long template =
            management.createTemplate(
                new DeliveryExpressTemplateCreateCmd(
                    "按件运费",
                    1,
                    0,
                    List.of(
                        new DeliveryChargeCmd(
                            null,
                            List.of(110101),
                            BigDecimal.ONE,
                            500,
                            BigDecimal.ONE,
                            150)),
                    List.of()));
        var categories = mallComponents.get(ProductCategoryGateway.class);
        var brands = mallComponents.get(ProductBrandGateway.class);
        var category =
            categories.saveCategory(new ProductCategory(null, null, "快递分类", null, 0, "ENABLED"));
        var brand = brands.saveBrand(new ProductBrand(null, "快递品牌", null, null, 0, "ENABLED"));
        var spu =
            mallComponents
                .get(ProductSpuGateway.class)
                .insertSpu(
                    new ProductSpu(
                        null,
                        "DELIVERY-SPU",
                        "快递商品",
                        category.id(),
                        brand.id(),
                        null,
                        null,
                        "ON_SHELF",
                        List.of(),
                        List.of(1),
                        template,
                        null,
                        null,
                        null,
                        null));
        mallComponents
            .get(ProductSkuMapper.class)
            .update(
                null,
                Wrappers.<ProductSkuDO>lambdaUpdate()
                    .eq(ProductSkuDO::getId, sku)
                    .set(ProductSkuDO::getSpuId, spu.id())
                    .set(ProductSkuDO::getWeight, new BigDecimal("1.25"))
                    .set(ProductSkuDO::getVolume, new BigDecimal("0.1")));
        return template;
    }

    @Test
    void deliveryQuotePersistsRecipientAndFreightAndPartialRefundUsesFrozenPaidLine()
        throws Exception {
        deliveryPointGift = true;
        configureDeliveryProduct();
        skuProperties(List.of(new ProductSkuSpec(null, "尺码", null, "L")));
        cartService.saveCart(1L, sku, 2, true);
        var service = new SettlementService(checkoutService, orderRpc, placement, settlements);
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
        assertEquals("尺码", saved.items().get(0).specs().get(0).specName());
        assertEquals("L", saved.items().get(0).specs().get(0).specValueName());
        deliveryAddress = new MemberAddressDTO(7L, 1L, "新收件人", "13800000000", 110101L, "修改后的地址");
        assertEquals(receipt, service.submit(1L, cached.settlementId(), "DELIVERY-ORDER"));
        var payment = paymentCreate.execute(receipt.orderNo(), "ALIPAY");
        notify.execute(
            new VerifiedPaymentNotification(
                payment.paymentNo(),
                receipt.orderNo(),
                "ALIPAY",
                "DELIVERY-PAY",
                saved.totalAmount(),
                "DELIVERY-NOTICE"));
        var shipped =
            fulfillmentService.ship(
                new ShipmentCreateCmd(
                    receipt.orderNo(),
                    "SHIP-FROZEN",
                    "SF",
                    "TRACK-FROZEN",
                    null,
                    null,
                    null));
        assertEquals("原始地址", shipped.address());
        assertEquals("收件人", shipped.recipientName());
        var afterSales = tradeComponents.get(AfterSaleService.class);
        var line = afterSales.items(receipt.orderNo()).get(0);
        assertEquals(new BigDecimal("26.50"), line.refundableAmount());
        var sale =
            afterSales.create(
                new AfterSaleCreateCmd(
                    receipt.orderNo(),
                    "DELIVERY-REFUND",
                    line.orderItemId(),
                    1,
                    "REFUND_ONLY",
                    "部分退货"));
        assertEquals(new BigDecimal("13.25"), sale.amount());
        afterSales.agree(sale.refundNo());
        Mockito.when(refundClient.refund(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(call -> refundSuccess(call.getArgument(0)));
        var result = tradeComponents.get(PaymentRefundCreateCmdExe.class).execute(sale.refundNo());
        assertEquals("SUCCESS", result.status());
        assertEquals(new BigDecimal("13.25"), result.amount());
        assertEquals(
            "原始地址",
            orderPersistenceService
                .findByOrderNo(receipt.orderNo())
                .delivery()
                .receiverDetailAddress());
    }

    @Test
    void foreignAddressAndChangedAddressOrFreightCannotBeSubmittedFromOldQuote() throws Exception {
        Long templateId = configureDeliveryProduct();
        cartService.saveCart(1L, sku, 2, true);
        var service = new SettlementService(checkoutService, orderRpc, placement, settlements);
        assertThrows(
            ServiceException.class,
            () -> service.create(new SettlementQuoteQry(1L, List.of(sku), null, false, 8L)));
        var original = service.create(new SettlementQuoteQry(1L, List.of(sku), null, false, 7L));
        deliveryAddress = new MemberAddressDTO(7L, 1L, "收件人", "13800000000", 110101L, "预览后改地址");
        assertThrows(
            ServiceException.class,
            () -> service.submit(1L, original.settlementId(), "CHANGED-ADDRESS"));
        assertEquals(0, count("order_info"));
        assertEquals(10, productSkuGateway.findById(sku).stock());
        var fresh = service.create(new SettlementQuoteQry(1L, List.of(sku), null, false, 7L));
        tradeComponents
            .get(DeliveryExpressTemplateChargeMapper.class)
            .update(
                null,
                Wrappers.<DeliveryExpressTemplateChargeDO>lambdaUpdate()
                    .eq(DeliveryExpressTemplateChargeDO::getTemplateId, templateId)
                    .set(DeliveryExpressTemplateChargeDO::getStartPrice, 700));
        var conflict =
            assertThrows(
                ServiceException.class,
                () -> service.submit(1L, fresh.settlementId(), "CHANGED-FREIGHT"));
        assertEquals(409, conflict.getCode());
        assertEquals(0, count("order_info"));
    }

    @Test
    void disabledCompanyBlocksNewShipmentButNeverBlocksExistingShipmentReplay() throws Exception {
        var order = paidOrder(null);
        var companies = tradeComponents.get(DeliveryExpressGateway.class);
        var company = companies.byCode("SF");
        companies.update(
            new DeliveryExpress(
                company.id(),
                company.code(),
                company.name(),
                company.logo(),
                company.sort(),
                1));
        assertThrows(
            ServiceException.class, () -> fulfillmentService.ship(shipment(order.orderNo())));
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertNull(shipmentGateway.shipment(order.orderNo()));
        companies.update(
            new DeliveryExpress(
                company.id(),
                company.code(),
                company.name(),
                company.logo(),
                company.sort(),
                0));
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
        assertEquals(
            received.receivedAt(),
            tradeComponents
                .get(TradeOrderMapper.class)
                .selectById(order.id())
                .getReceiveTime());
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
            () -> requestAfterSale(new RefundRequestCmd(order.orderNo(), "R1", "不同原因")));
        assertThrows(
            IllegalStateException.class,
            () -> requestAfterSale(new RefundRequestCmd(order.orderNo(), "R2", "第二申请")));
        assertThrows(
            IllegalStateException.class,
            () -> fulfillmentService.ship(shipment(order.orderNo())));
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals("SUCCESS", payments.findByPaymentNo(request.paymentNo()).status());
        assertEquals("USED", couponGateway.coupon(1L, coupon.id(), false).status());
        assertEquals(8, productSkuGateway.findById(sku).stock());
        var rejected = rejectAfterSale(new RefundRejectCmd(request.refundNo(), "核验后驳回"));
        assertEquals("REJECTED", rejected.status());
        assertEquals(rejected, rejectAfterSale(new RefundRejectCmd(request.refundNo(), "核验后驳回")));
        assertEquals(rejected, requestAfterSale(cmd));
        assertFalse(refundRequestGateway.activeRefund(order.orderNo()));
        assertThrows(
            IllegalStateException.class,
            () -> rejectAfterSale(new RefundRejectCmd(request.refundNo(), "改写审核")));
        fulfillmentService.ship(shipment(order.orderNo()));
    }

    @Test
    void rejectedRequestAllowsNewKeyButNeverReopensOriginalRequest() throws Exception {
        var order = paidOrder(null);
        var first = requestAfterSale(new RefundRequestCmd(order.orderNo(), "R1", "原因"));
        rejectAfterSale(new RefundRejectCmd(first.refundNo(), "驳回"));
        var second = requestAfterSale(new RefundRequestCmd(order.orderNo(), "R2", "补充材料"));
        assertNotEquals(first.refundNo(), second.refundNo());
        assertEquals("REQUESTED", second.status());
        assertEquals(2, count("order_after_sale"));
    }

    @Test
    void refundRequiresExactlyOneMatchingSuccessfulPayment() throws Exception {
        var order = paidOrder(null);
        sql(
            tradeSource,
            "UPDATE pay_payment SET amount=1.00 WHERE order_no='" + order.orderNo() + "'");
        assertThrows(
            IllegalStateException.class,
            () -> requestAfterSale(new RefundRequestCmd(order.orderNo(), "R1", "原因")));
        sql(
            tradeSource,
            "UPDATE pay_payment SET amount=20.00 WHERE order_no='" + order.orderNo() + "'");
        sql(
            tradeSource,
            "INSERT INTO pay_payment(payment_no,order_no,channel,amount,status)"
                + " VALUES('DUP','"
                + order.orderNo()
                + "','WECHAT',20.00,'SUCCESS')");
        assertThrows(
            IllegalStateException.class,
            () -> requestAfterSale(new RefundRequestCmd(order.orderNo(), "R1", "原因")));
        assertEquals(0, count("order_after_sale"));
    }

    @Test
    void concurrentShipmentAndAfterSaleSerializeAndPreserveValidFulfillmentState()
        throws Exception {
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
                        requestAfterSale(new RefundRequestCmd(order.orderNo(), "R1", "原因"));
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
        var request = requestAfterSale(new RefundRequestCmd(order.orderNo(), "R1", "原因"));
        var pages = new OrderPageQueryExe(tradeComponents.get(TradeOrderGateway.class));
        var page = pages.execute(new OrderPageQry(order.orderNo(), "PAID", 1, 20));
        assertEquals(1, page.total());
        assertTrue(page.records().get(0).activeRefund());
        assertTrue(page.records().get(0).inventoryConfirmed());
        var refunds = new AfterSalePageQueryExe(tradeComponents.get(AfterSaleGateway.class));
        assertEquals(
            request.refundNo(),
            refunds.execute(new AfterSalePageQry(order.orderNo(), "REQUESTED", 1, 20))
                .getRecords()
                .get(0)
                .refundNo());
        assertThrows(
            IllegalArgumentException.class,
            () -> pages.execute(new OrderPageQry(null, "UNKNOWN", 1, 20)));
        assertThrows(
            IllegalArgumentException.class,
            () -> refunds.execute(new AfterSalePageQry(null, null, 0, 20)));
        assertThrows(
            IllegalArgumentException.class, () -> fulfillmentService.receive(order.orderNo()));
    }

    @Test
    void ambiguousStockConfirmCannotUnlockShipmentUntilRetryReturnsSuccess() throws Exception {
        var order = place("CONFIRM-LOST", null);
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        var notice =
            new VerifiedPaymentNotification(
                payment.paymentNo(),
                order.orderNo(),
                "ALIPAY",
                "CH-LOST",
                order.totalAmount(),
                "NOTICE-LOST");
        loseNextConfirmResponse = true;
        assertThrows(IllegalStateException.class, () -> notify.execute(notice));
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertFalse(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
        assertThrows(
            IllegalStateException.class,
            () -> fulfillmentService.ship(shipment(order.orderNo())));
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
        Mockito.doReturn(false).when(failure).transitionStatus(order.orderNo(), "PAID", "SHIPPED");
        var failing =
            tx(
                new OrderFulfillmentService(
                    tradeComponents.get(InventoryConfirmationGateway.class),
                    tradeComponents.get(OrderPersistenceService.class),
                    tradeComponents.get(AfterSaleGateway.class),
                    tradeComponents.get(ShipmentGateway.class),
                    failure,
                    tradeComponents.get(DeliveryExpressGateway.class),
                    tradeComponents.get(OrderLogService.class),
                    tradeComponents.bean(
                        "com.lemnt.order.domain.event.gateway.OrderEventGateway")),
                tradeSource);
        assertThrows(IllegalStateException.class, () -> failing.ship(shipment(order.orderNo())));
        assertNull(shipmentGateway.shipment(order.orderNo()));
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        fulfillmentService.ship(shipment(order.orderNo()));
        Mockito.doReturn(false)
            .when(failure)
            .completeReceipt(Mockito.eq(order.orderNo()), Mockito.any());
        assertThrows(IllegalStateException.class, () -> failing.receive(order.orderNo()));
        assertNull(shipmentGateway.shipment(order.orderNo()).receivedAt());
        assertNull(
            tradeComponents
                .get(TradeOrderMapper.class)
                .selectById(order.id())
                .getReceiveTime());
        assertEquals("SHIPPED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void schemaDoesNotInferStockConfirmationFromPaidStatus() throws Exception {
        var order = paidOrder(null);
        assertTrue(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
        sql(tradeSource, "DROP TABLE pay_payment_refund");
        sql(tradeSource, "DROP TABLE order_after_sale_log");
        sql(tradeSource, "DROP TABLE order_promotion_refund");
        sql(tradeSource, "DROP TABLE order_after_sale");
        sql(tradeSource, "DROP TABLE order_shipment");
        sql(tradeSource, "DROP TABLE order_inventory_confirmation");
        ScriptUtils.executeSqlScript(
            tradeSchema, new ClassPathResource("order-sql/lemnt_order.sql"));

        var script =
            new ClassPathResource("order-sql/lemnt_order.sql")
                .getContentAsString(StandardCharsets.UTF_8);
        sql(
            tradeSource,
            script.substring(
                script.indexOf("CREATE TABLE IF NOT EXISTS order_promotion_refund"),
                script.indexOf(
                    ";",
                    script.indexOf(
                        "CREATE TABLE IF NOT EXISTS order_promotion_refund"))));
        assertFalse(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
        remotes.paid(order.orderNo());
        assertTrue(inventoryConfirmationGateway.inventoryConfirmed(order.orderNo()));
    }

    @Test
    void consumerReceiptChecksOwnerAgainstPersistedOrderAndCheckout() {
        var order = place("APP-OWNER", null);
        var query =
            new SettlementReceiptQueryExe(
                mallComponents.get(SettlementRecordService.class), orderRpc);
        assertEquals(order.orderNo(), query.execute(1L, order.orderNo()).orderNo());
        assertNull(query.execute(2L, order.orderNo()));
        assertNull(query.execute(1L, "MISSING"));
    }

    Settlement settlement() {
        var value = placement.prepareSettlement(new SettlementQuoteQry(1L, List.of(sku), null));
        settlements.cache(value);
        return value;
    }

    /**
     * 报价仅缓存；订单已提交但响应丢失时，同一请求只能产生一张订单。
     */
    @Test
    void redisSettlementSubmissionRetriesAfterLostOrderResponse() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, BigDecimal.ONE).id());
        var value =
            placement.prepareSettlement(new SettlementQuoteQry(1L, List.of(sku), coupon.id()));
        settlements.cache(value);
        assertEquals(0, count("order_info"));
        assertEquals(0, cacheStore.submissions.size());
        loseNextOrderResponse = true;
        assertThrows(
            IllegalStateException.class,
            () -> placement.submitSettlement(1L, value.id(), "LOST-ORDER-ACK", settlements));
        assertEquals(1, count("order_info"));

        settlementCache.clear();
        var order = placement.submitSettlement(1L, value.id(), "LOST-ORDER-ACK", settlements);
        assertEquals(
            order.orderNo(),
            placement
                .submitSettlement(1L, value.id(), "LOST-ORDER-ACK", settlements)
                .orderNo());
        assertEquals(1, count("order_info"));
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
        assertEquals(1, count("order_info"));
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
        assertEquals(0, count("order_info"));
    }

    @Test
    void settlementUsesCachedQuantityAndPreservesLaterCartEdits() {
        var value = settlement();
        cartService.saveCart(1L, sku, 3, true);
        var order = placement.submitSettlement(1L, value.id(), "S1", settlements);
        assertEquals(
            2,
            orderPersistenceService.findByOrderNo(order.orderNo()).items().get(0).quantity());
        worker().execute(50);
        assertEquals(3, cartGateway.cart(1L).get(0).quantity());
    }

    @Test
    void settlementRejectsChangedPriceWithoutMutatingSnapshotOrOrder() throws Exception {
        var value = settlement();
        sql(mallSource, "UPDATE product_sku SET price=99.00 WHERE id=" + sku);
        assertThrows(
            ServiceException.class,
            () -> placement.submitSettlement(1L, value.id(), "S1", settlements));
        assertEquals(
            new BigDecimal("20.00"), settlementCache.get(value.id()).quote().payableAmount());
        assertEquals(0, count("order_info"));
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
            assertEquals(1, count("order_info"));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void settlementBindingFailurePreventsRemoteOrderCreation() throws Exception {
        var value = settlement();
        var failing =
            Mockito.mock(
                SettlementGateway.class,
                AdditionalAnswers.delegatesTo(settlementOrderGateway));
        Mockito.doThrow(new IllegalStateException("bind failed"))
            .when(failing)
            .bind(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString());
        var failedPlacement =
            new OrderPlacementService(
                tx(
                    new SettlementPreparationService(
                        mallComponents.get(PromotionPricingGateway.class),
                        mallComponents.get(SettlementDeliveryService.class),
                        mallComponents.get(CartSnapshotGateway.class),
                        mallComponents.bean(
                            "com.lemnt.order.domain.member.gateway.MemberLockGateway"),
                        mallComponents.get(SettlementRecordService.class),
                        orderRpc,
                        failing,
                        bridge,
                        new MemberValidationService(
                            id -> new MemberSnapshot(id, 1))),
                    tradeSource),
                orderRpc);
        assertThrows(
            IllegalStateException.class,
            () -> failedPlacement.submitSettlement(1L, value.id(), "S1", settlements));
        assertEquals(0, count("order_info"));
        assertEquals(0, cacheStore.submissions.size());

        assertNotNull(settlementCache.get(value.id()));
        assertNotNull(placement.submitSettlement(1L, value.id(), "S1", settlements));
    }

    @Test
    void cachedRequestRecoveryIsOwnerScopedAndIndependentOfCache() {
        var value = settlement();
        var order = placement.submitSettlement(1L, value.id(), "RECOVER", settlements);
        settlementCache.clear();
        var cache = Mockito.mock(SettlementGateway.class);
        var service =
            new SettlementService(
                mallComponents.get(SettlementRecordService.class),
                orderRpc,
                placement,
                cache);
        assertEquals(order.orderNo(), service.findByRequest(1L, "RECOVER").orderNo());
        assertNull(service.findByRequest(2L, "RECOVER"));
        assertNull(service.findByRequest(1L, "MISSING"));
        Mockito.verifyNoInteractions(cache);
    }

    @Test
    void legacyRequestRecoveryDoesNotCreateAnotherOrder() throws Exception {
        var order = place("LEGACY-RECOVER", null);
        var service =
            new SettlementService(
                mallComponents.get(SettlementRecordService.class),
                orderRpc,
                placement,
                settlements);
        assertEquals(order.orderNo(), service.findByRequest(1L, "LEGACY-RECOVER").orderNo());
        assertEquals(1, count("order_info"));
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
        assertEquals(1, count("order_info"));
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
        assertEquals(2, count("order_info"));
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
        assertEquals(0, count("order_info"));
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
        assertEquals(
            "PENDING_PAYMENT", orderPersistenceService.findByOrderNo(order.orderNo()).status());
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
                assertEquals(0, count("order_info"));
                assertEquals(1, cacheStore.abandoned.size());
            } else {
                assertNotNull(result);
                assertEquals(order.orderNo(), result.orderNo());
                assertEquals(1, count("order_info"));
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
            Mockito.mock(SettlementGateway.class, AdditionalAnswers.delegatesTo(settlements));
        Mockito.doThrow(new IllegalStateException("Redis down"))
            .when(gateway)
            .cached(ArgumentMatchers.anyString());
        assertNull(placement.abandonSettlement(1L, value.id(), "A", gateway));
        Mockito.verify(gateway, Mockito.never()).cached(ArgumentMatchers.anyString());
    }

    @Test
    void abandonWriteFailureRollsBackMarkerAndAllowsOriginalSubmit() {
        var value = settlement();
        var gateway =
            Mockito.mock(
                SettlementGateway.class,
                AdditionalAnswers.delegatesTo(settlementAbandonGateway));
        Mockito.doAnswer(
                call -> {
                    settlementAbandonGateway.abandon(
                        call.getArgument(0), call.getArgument(1));
                    throw new IllegalStateException("commit failure");
                })
            .when(gateway)
            .abandon(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString());
        assertThrows(
            IllegalStateException.class,
            () ->
                placementWith(settlementOrderGateway, gateway)
                    .abandonSettlement(1L, value.id(), "A", settlements));
        assertFalse(settlementAbandonGateway.abandoned(1L, value.id()));
        assertNotNull(placement.submitSettlement(1L, value.id(), "A", settlements));
    }

    @Test
    void memberPaymentStatusReflectsVerifiedLocalPaymentAcrossFulfillment() {
        var order = paidOrder(null);
        var query =
            new MemberPaymentStatusQueryExe(
                tradeComponents.bean(
                    "com.lemnt.pay.domain.order.gateway.OrderPaymentGateway"),
                payments);
        var key = new MemberPaymentStatusQry(1L, order.orderNo());
        assertEquals("SUCCESS", query.execute(key).localPaymentStatus());
        assertFalse(query.execute(key).channelVerified());
        worker().execute(50);
        fulfillmentService.ship(shipment(order.orderNo()));
        assertEquals("SHIPPED", query.execute(key).orderStatus());
        fulfillmentService.receive(order.orderNo());
        assertEquals("COMPLETED", query.execute(key).orderStatus());
        assertEquals("SUCCESS", query.execute(key).localPaymentStatus());
        assertNull(query.execute(new MemberPaymentStatusQry(2L, order.orderNo())));
    }

    @Test
    void inconsistentPersistedPaymentIsReportedWithoutChangingOrderOrInventory() throws Exception {
        var order = paidOrder(null);
        var stock = productSkuGateway.findById(sku).stock();
        sql(
            tradeSource,
            "UPDATE pay_payment SET amount=0.01 WHERE order_no='" + order.orderNo() + "'");
        var query =
            new MemberPaymentStatusQueryExe(
                tradeComponents.bean(
                    "com.lemnt.pay.domain.order.gateway.OrderPaymentGateway"),
                payments);
        assertEquals(
            "RECONCILE_REQUIRED",
            query.execute(new MemberPaymentStatusQry(1L, order.orderNo()))
                .localPaymentStatus());
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals(stock, productSkuGateway.findById(sku).stock());
    }

    @Test
    void publicClaimDefaultsClosedAndDoesNotExposePrivateTemplates() {
        var t = campaign(2, new BigDecimal("2.00"));
        assertThrows(ServiceException.class, () -> coupons.claim(new CouponClaimCmd(1L, t.id())));
        assertEquals(0, coupons.offers(new CouponPageQry(1L, 1, 20)).total());
        coupons.publicClaim(new CouponPublicClaimCmd(t.id(), true));
        assertEquals(1, coupons.offers(new CouponPageQry(1L, 1, 20)).total());
        var c = coupons.claim(new CouponClaimCmd(1L, t.id()));
        assertThrows(ServiceException.class, () -> coupons.claim(new CouponClaimCmd(1L, t.id())));
        assertEquals(1, couponTemplateGateway.template(t.id()).issuedCount());
        coupons.publicClaim(new CouponPublicClaimCmd(t.id(), false));
        assertThrows(ServiceException.class, () -> coupons.claim(new CouponClaimCmd(1L, t.id())));
        assertThrows(ServiceException.class, () -> coupons.claim(new CouponClaimCmd(2L, t.id())));
        var wallet = coupons.wallet(new CouponPageQry(1L, 1, 20));
        assertEquals(1, wallet.total());
        assertTrue(wallet.records().get(0).usable());
        assertNotNull(wallet.records().get(0).terms());
        assertEquals(0, coupons.wallet(new CouponPageQry(2L, 1, 20)).total());
    }

    @Test
    void publicClaimConcurrentQuotaHasExactlyOneWinner() throws Exception {
        var t = campaign(1, BigDecimal.ONE);
        coupons.publicClaim(new CouponPublicClaimCmd(t.id(), true));
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
                                coupons.claim(new CouponClaimCmd(member, t.id()));
                                return true;
                            } catch (ServiceException ex) {
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
        coupons.publicClaim(new CouponPublicClaimCmd(t.id(), true));
        coupons.claim(new CouponClaimCmd(1L, t.id()));
        couponTemplateGateway.enableTemplate(t.id(), false);
        assertEquals(0, coupons.offers(new CouponPageQry(1L, 1, 20)).total());
        assertTrue(coupons.wallet(new CouponPageQry(1L, 1, 20)).records().get(0).usable());
        couponTemplateGateway.enableTemplate(t.id(), true);
        try (var st = mallSchema.createStatement()) {
            st.execute(
                "UPDATE promotion_coupon_template SET"
                    + " valid_until=DATEADD('MINUTE',-1,CURRENT_TIMESTAMP) WHERE id="
                    + t.id());
        }
        assertThrows(ServiceException.class, () -> coupons.claim(new CouponClaimCmd(2L, t.id())));
        assertTrue(coupons.wallet(new CouponPageQry(1L, 1, 20)).records().get(0).usable());
    }

    /**
     * 最终数量重复设置不累加，不依赖请求记录表。
     */
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

    /**
     * 超库存写入失败后，原购物车数量保持不变。
     */
    @Test
    void cartQuantityRejectsStockOverflowWithoutChangingCart() {
        assertThrows(
            IllegalStateException.class,
            () -> cartAdditions.execute(new CartSaveCmd(1L, sku, 11, true)));
        assertEquals(2, cartGateway.cart(1L).get(0).quantity());
        assertEquals(3, cartAdditions.execute(new CartSaveCmd(1L, sku, 3, true)).quantity());
    }

    /**
     * 并发设置相同最终数量不会累加。
     */
    @Test
    void concurrentSameCartQuantityDoesNotAccumulate() throws Exception {
        var pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> task =
                () -> cartAdditions.execute(new CartSaveCmd(1L, sku, 3, true)).quantity();
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
        coupons.publicClaim(new CouponPublicClaimCmd(t.id(), true));
        try (var st = mallSchema.createStatement()) {
            st.execute(
                "ALTER TABLE promotion_coupon ADD CONSTRAINT reject_claim CHECK(member_id<2)");
        }
        var c = new CouponClaimCmd(2L, t.id());
        assertThrows(RuntimeException.class, () -> coupons.claim(c));
        assertEquals(0, couponTemplateGateway.template(t.id()).issuedCount());
        try (var st = mallSchema.createStatement()) {
            st.execute("ALTER TABLE promotion_coupon DROP CONSTRAINT reject_claim");
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
                        return cartAdditions.execute(new CartSaveCmd(1L, sku, 2, true));
                    });
            var b =
                pool.submit(
                    () -> {
                        start.await();
                        return cartAdditions.execute(new CartSaveCmd(1L, sku, 3, true));
                    });
            start.countDown();
            a.get(10, TimeUnit.SECONDS);
            b.get(10, TimeUnit.SECONDS);
            assertTrue(Set.of(2, 3).contains(cartGateway.cart(1L).get(0).quantity()));
        } finally {
            pool.shutdownNow();
        }
    }

    private OrderPlacementService placementWith(
        SettlementGateway orderGateway, SettlementGateway abandonGateway) {
        return new OrderPlacementService(
            tx(
                new SettlementPreparationService(
                    mallComponents.get(PromotionPricingGateway.class),
                    mallComponents.get(SettlementDeliveryService.class),
                    mallComponents.get(CartSnapshotGateway.class),
                    mallComponents.bean(
                        "com.lemnt.order.domain.member.gateway.MemberLockGateway"),
                    mallComponents.get(SettlementRecordService.class),
                    orderRpc,
                    (orderGateway != settlements ? orderGateway : abandonGateway),
                    bridge,
                    new MemberValidationService(id -> new MemberSnapshot(id, 1))),
                tradeSource),
            orderRpc);
    }

    PaymentRefundResult refundSuccess(PaymentRefund refund) {
        return new PaymentRefundResult(
            refund.refundNo(),
            refund.paymentNo(),
            refund.channel(),
            refund.amount(),
            "SUCCESS",
            "CHANNEL-" + refund.refundNo());
    }

    @Test
    void afterSalePaymentRefundUsesOriginalPaymentAndSuccessfulReplayNeverCallsChannelAgain()
        throws Exception {
        var order = paidOrder(null);
        var afterSale =
            approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-1", "不再需要"));
        Mockito.when(refundClient.refund(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(
                call -> {
                    assertFalse(
                        TransactionSynchronizationManager.isActualTransactionActive());
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
        assertEquals(1, count("pay_payment_refund"));
        Mockito.verify(refundClient, Mockito.times(1))
            .refund(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        assertThrows(
            IllegalStateException.class,
            () -> fulfillmentService.ship(shipment(order.orderNo())));
        var payment = payments.findByPaymentNo(result.paymentNo());
        notify.execute(
            new VerifiedPaymentNotification(
                payment.paymentNo(),
                payment.orderNo(),
                payment.channel(),
                payment.channelTradeNo(),
                payment.amount(),
                payment.notifyId()));
        assertEquals("REFUNDED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        var report =
            tradeComponents
                .get(MemberPaymentStatusQueryExe.class)
                .execute(new MemberPaymentStatusQry(1L, order.orderNo()));
        assertEquals("SUCCESS", report.localPaymentStatus());
        assertEquals("REFUNDED", report.orderStatus());
    }

    @Test
    void lostRefundResponsePreservesOneRefundAndOriginalKeyIsQueriedOnRetry() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-2", "原因"));
        Mockito.when(refundClient.refund(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenThrow(new IllegalStateException("渠道响应丢失"));
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        assertThrows(IllegalStateException.class, () -> create.execute(afterSale.refundNo()));
        var original =
            tradeComponents
                .get(PaymentRefundGateway.class)
                .findByAfterSaleNo(afterSale.refundNo());
        assertEquals("WAITING", original.status());
        assertEquals("REFUNDING", refundRequestGateway.afterSale(afterSale.refundNo()).status());
        assertTrue(refundRequestGateway.activeRefund(order.orderNo()));
        assertThrows(
            IllegalStateException.class,
            () -> fulfillmentService.ship(shipment(order.orderNo())));
        assertThrows(
            IllegalStateException.class,
            () -> rejectAfterSale(new RefundRejectCmd(afterSale.refundNo(), "驳回")));
        Mockito.when(refundClient.refundQuery(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(call -> refundSuccess(call.getArgument(0)));
        var result = create.execute(afterSale.refundNo());
        assertEquals(original.refundNo(), result.refundNo());
        assertEquals("SUCCESS", result.status());
        assertEquals(1, count("pay_payment_refund"));
        Mockito.verify(refundClient, Mockito.times(1))
            .refund(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void acceptedRefundIsNeverResubmittedWhenChannelQueryTemporarilyCannotFindIt()
        throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-3", "原因"));
        Mockito.when(refundClient.refund(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(
                call -> {
                    PaymentRefund refund = call.getArgument(0);
                    return new PaymentRefundResult(
                        refund.refundNo(),
                        refund.paymentNo(),
                        refund.channel(),
                        refund.amount(),
                        "WAITING",
                        "ACCEPTED");
                });
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        var pending = create.execute(afterSale.refundNo());
        assertEquals(pending, create.execute(afterSale.refundNo()));
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        Mockito.verify(refundClient, Mockito.times(1))
            .refund(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void invalidRefundBindingNeverMarksSuccessOrReleasesFulfillmentBlock() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-4", "原因"));
        Mockito.when(refundClient.refund(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(
                call -> {
                    PaymentRefund refund = call.getArgument(0);
                    return new PaymentRefundResult(
                        refund.refundNo(),
                        refund.paymentNo(),
                        refund.channel(),
                        BigDecimal.ONE,
                        "SUCCESS",
                        "WRONG-AMOUNT");
                });
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        assertThrows(IllegalStateException.class, () -> create.execute(afterSale.refundNo()));
        var local =
            tradeComponents
                .get(PaymentRefundGateway.class)
                .findByAfterSaleNo(afterSale.refundNo());
        assertEquals("WAITING", local.status());
        assertEquals("REFUNDING", refundRequestGateway.afterSale(afterSale.refundNo()).status());
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void fundsSuccessSurvivesOrderRollbackAndOriginalRefundRepairsOrder() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-5", "原因"));
        Mockito.when(refundClient.refund(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(call -> refundSuccess(call.getArgument(0)));
        sql(
            tradeSource,
            "ALTER TABLE order_info ADD CONSTRAINT test_refund_failure CHECK(status <> 'REFUNDED')");
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        assertThrows(RuntimeException.class, () -> create.execute(afterSale.refundNo()));
        assertEquals(
            "SUCCESS",
            tradeComponents
                .get(PaymentRefundGateway.class)
                .findByAfterSaleNo(afterSale.refundNo())
                .status());
        assertEquals("REFUNDING", refundRequestGateway.afterSale(afterSale.refundNo()).status());
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        sql(tradeSource, "ALTER TABLE order_info DROP CONSTRAINT test_refund_failure");
        Mockito.when(refundClient.refundQuery(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(call -> refundSuccess(call.getArgument(0)));
        assertEquals("SUCCESS", create.execute(afterSale.refundNo()).status());
        events.drainOrThrow();
        assertEquals("REFUNDED", refundRequestGateway.afterSale(afterSale.refundNo()).status());
        assertEquals("REFUNDED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals(
            2,
            orderPersistenceService
                .findByOrderNo(order.orderNo())
                .items()
                .get(0)
                .refundedQuantity());
        Mockito.verify(refundClient, Mockito.times(1))
            .refund(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void rejectedOrMissingAfterSaleAndMismatchedPaymentCannotCreateRefund() throws Exception {
        var order = paidOrder(null);
        var afterSale = requestAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-6", "原因"));
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        assertThrows(IllegalArgumentException.class, () -> create.execute("missing"));
        sql(
            tradeSource,
            "UPDATE pay_payment SET amount=0.01 WHERE payment_no='"
                + afterSale.paymentNo()
                + "'");
        assertThrows(IllegalStateException.class, () -> create.execute(afterSale.refundNo()));
        sql(
            tradeSource,
            "UPDATE pay_payment SET amount="
                + afterSale.amount()
                + " WHERE payment_no='"
                + afterSale.paymentNo()
                + "'");
        rejectAfterSale(new RefundRejectCmd(afterSale.refundNo(), "拒绝退款"));
        assertThrows(IllegalStateException.class, () -> create.execute(afterSale.refundNo()));
        assertEquals(0, count("pay_payment_refund"));
        Mockito.verify(refundClient, Mockito.never())
            .refund(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(refundClient, Mockito.never())
            .refundQuery(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void concurrentRefundCallsUseOneDurableRefundNumber() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-7", "原因"));
        var numbers = ConcurrentHashMap.<String>newKeySet();
        Mockito.when(refundClient.refund(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(
                call -> {
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
        assertEquals(1, count("pay_payment_refund"));
    }

    @Test
    void paymentAndRefundPagesUseRealTableFiltersAndCommonPagination() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-8", "原因"));
        Mockito.when(refundClient.refund(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(call -> refundSuccess(call.getArgument(0)));
        var created =
            tradeComponents.get(PaymentRefundCreateCmdExe.class).execute(afterSale.refundNo());
        var paymentPage =
            tradeComponents
                .get(PaymentPageQueryExe.class)
                .execute(
                    new PaymentPageQry(
                        null, order.orderNo(), "ALIPAY", "SUCCESS", 1, 1));
        assertEquals(1, paymentPage.getTotal());
        assertEquals(afterSale.paymentNo(), paymentPage.getRecords().get(0).paymentNo());
        assertEquals(
            0,
            tradeComponents
                .get(PaymentPageQueryExe.class)
                .execute(new PaymentPageQry(null, order.orderNo(), "WECHAT", null, 1, 20))
                .getTotal());
        var refundQuery = tradeComponents.get(PaymentRefundPageQueryExe.class);
        var refundPage =
            refundQuery.execute(
                new PaymentRefundPageQry(
                    afterSale.refundNo(), created.paymentNo(), "SUCCESS", 1, 20));
        assertEquals(1, refundPage.getTotal());
        var queriedRefund = refundPage.getRecords().get(0);
        assertEquals(created.refundNo(), queriedRefund.refundNo());
        assertEquals(created.afterSaleNo(), queriedRefund.afterSaleNo());
        assertEquals(created.paymentNo(), queriedRefund.paymentNo());
        assertEquals(created.orderNo(), queriedRefund.orderNo());
        assertEquals(created.channel(), queriedRefund.channel());
        assertEquals(created.amount(), queriedRefund.amount());
        assertEquals(created.status(), queriedRefund.status());
        assertEquals(created.channelRefundNo(), queriedRefund.channelRefundNo());
        assertEquals(
            0,
            refundQuery
                .execute(
                    new PaymentRefundPageQry(
                        afterSale.refundNo(), null, "FAILED", 1, 20))
                .getTotal());
        var afterSalePage =
            new AfterSalePageQueryExe(refundRequestGateway)
                .execute(new AfterSalePageQry(order.orderNo(), "REFUNDED", 1, 20));
        assertEquals(1, afterSalePage.getTotal());
        var orderPage =
            tradeComponents
                .get(OrderPageQueryExe.class)
                .execute(new OrderPageQry(order.orderNo(), "REFUNDED", 1, 20));
        assertEquals(1, orderPage.total());
    }

    @Test
    void callerRollbackDoesNotEraseChannelRefundOrItsCommittedResult() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-9", "原因"));
        Mockito.when(refundClient.refund(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(call -> refundSuccess(call.getArgument(0)));
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        var transaction = new TransactionTemplate(new DataSourceTransactionManager(tradeSource));
        transaction.executeWithoutResult(
            status -> {
                assertEquals("SUCCESS", create.execute(afterSale.refundNo()).status());
                status.setRollbackOnly();
            });
        assertEquals(
            "SUCCESS",
            tradeComponents
                .get(PaymentRefundGateway.class)
                .findByAfterSaleNo(afterSale.refundNo())
                .status());
        assertEquals("REFUNDED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void confirmedChannelFailureIsRecordedWithoutPretendingOrderWasRefunded() throws Exception {
        var order = paidOrder(null);
        var afterSale = approvedAfterSale(new RefundRequestCmd(order.orderNo(), "REFUND-10", "原因"));
        Mockito.when(refundClient.refund(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(
                call -> {
                    PaymentRefund refund = call.getArgument(0);
                    return new PaymentRefundResult(
                        refund.refundNo(),
                        refund.paymentNo(),
                        refund.channel(),
                        refund.amount(),
                        "FAILED",
                        null);
                });
        var result =
            tradeComponents.get(PaymentRefundCreateCmdExe.class).execute(afterSale.refundNo());
        assertEquals("FAILED", result.status());
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals("REFUNDING", refundRequestGateway.afterSale(afterSale.refundNo()).status());
        assertTrue(refundRequestGateway.activeRefund(order.orderNo()));
    }

    AfterSaleCreateCmd itemRefund(String orderNo, String requestNo, int quantity, String type) {
        var line = tradeComponents.get(AfterSaleService.class).items(orderNo).get(0);
        return new AfterSaleCreateCmd(
            orderNo, requestNo, line.orderItemId(), quantity, type, "商品售后");
    }

    void successfulRefundChannel() {
        Mockito.when(refundClient.refund(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(call -> refundSuccess(call.getArgument(0)));
    }

    @Test
    void partialRefundsUseDiscountedItemQuotaAndOnlyCumulativeFullRefundClosesOrder()
        throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, new BigDecimal("3.00")).id());
        var order = paidOrder(coupon.id());
        var service = tradeComponents.get(AfterSaleService.class);
        var first = service.create(itemRefund(order.orderNo(), "PARTIAL-1", 1, "REFUND_ONLY"));
        assertEquals(new BigDecimal("8.50"), first.amount());
        assertEquals(
            first, service.create(itemRefund(order.orderNo(), "PARTIAL-1", 1, "REFUND_ONLY")));
        var createRefund = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        assertThrows(IllegalStateException.class, () -> createRefund.execute(first.refundNo()));
        service.agree(first.refundNo());
        sql(
            tradeSource,
            "UPDATE order_after_sale SET decided_at='2026-01-01 00:00:00' WHERE refund_no='"
                + first.refundNo()
                + "'");
        successfulRefundChannel();
        assertEquals("SUCCESS", createRefund.execute(first.refundNo()).status());
        assertEquals(
            LocalDateTime.of(2026, 1, 1, 0, 0),
            refundRequestGateway.afterSale(first.refundNo()).decidedAt());
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals(1, service.items(order.orderNo()).get(0).refundableQuantity());
        assertThrows(
            IllegalStateException.class,
            () ->
                approvedAfterSale(
                    new RefundRequestCmd(order.orderNo(), "LEGACY-FULL", "不能重复整单退")));
        var second = service.create(itemRefund(order.orderNo(), "PARTIAL-2", 1, "REFUND_ONLY"));
        assertEquals(new BigDecimal("8.50"), second.amount());
        service.agree(second.refundNo());
        createRefund.execute(second.refundNo());
        assertEquals("REFUNDED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals(0, service.items(order.orderNo()).get(0).refundableQuantity());
        assertEquals(2, count("pay_payment_refund"));
    }

    @Test
    void returnedGoodsMustBeApprovedRegisteredAndReceivedBeforeFinancialRefund() throws Exception {
        var order = paidOrder(null);
        var service = tradeComponents.get(AfterSaleService.class);
        assertThrows(
            IllegalStateException.class,
            () ->
                service.create(
                    itemRefund(
                        order.orderNo(),
                        "RETURN-BEFORE-SHIP",
                        1,
                        "RETURN_REFUND")));
        fulfillmentService.ship(shipment(order.orderNo()));
        var sale = service.create(itemRefund(order.orderNo(), "RETURN-1", 1, "RETURN_REFUND"));
        var refund = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        assertEquals("AWAITING_RETURN", service.agree(sale.refundNo()).status());
        assertThrows(IllegalStateException.class, () -> refund.execute(sale.refundNo()));
        var registered =
            service.registerReturn(new AfterSaleReturnCmd(sale.refundNo(), "SF", "RET-1"));
        assertEquals("RETURNED", registered.status());
        assertEquals(
            registered,
            service.registerReturn(new AfterSaleReturnCmd(sale.refundNo(), "SF", "RET-1")));
        assertThrows(
            IllegalStateException.class,
            () ->
                service.registerReturn(
                    new AfterSaleReturnCmd(sale.refundNo(), "SF", "OTHER")));
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
        assertThrows(
            IllegalStateException.class,
            () ->
                tradeComponents
                    .get(PaymentRefundCreateCmdExe.class)
                    .execute(first.refundNo()));
        fulfillmentService.ship(shipment(order.orderNo()));
        var returned =
            service.create(itemRefund(order.orderNo(), "REFUSE-ITEM", 1, "RETURN_REFUND"));
        service.agree(returned.refundNo());
        service.registerReturn(new AfterSaleReturnCmd(returned.refundNo(), "SF", "RET-REFUSE"));
        var refused = service.refuse(new AfterSaleRefuseCmd(returned.refundNo(), "商品不符"));
        assertEquals("RETURN_REJECTED", refused.status());
        assertEquals(2, service.items(order.orderNo()).get(0).refundableQuantity());
        assertThrows(IllegalStateException.class, () -> service.receive(returned.refundNo()));
        assertThrows(
            IllegalStateException.class,
            () ->
                tradeComponents
                    .get(PaymentRefundCreateCmdExe.class)
                    .execute(returned.refundNo()));
        Mockito.verify(refundClient, Mockito.never())
            .refund(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(refundClient, Mockito.never())
            .refundQuery(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void concurrentItemRequestsNeverReserveMoreThanPurchasedQuantity() throws Exception {
        var order = paidOrder(null);
        var service = tradeComponents.get(AfterSaleService.class);
        var cmd1 = itemRefund(order.orderNo(), "CONCURRENT-A", 2, "REFUND_ONLY");
        var cmd2 = itemRefund(order.orderNo(), "CONCURRENT-B", 2, "REFUND_ONLY");
        var pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> a =
                () -> {
                    try {
                        service.create(cmd1);
                        return true;
                    } catch (IllegalStateException expected) {
                        return false;
                    }
                };
            Callable<Boolean> b =
                () -> {
                    try {
                        service.create(cmd2);
                        return true;
                    } catch (IllegalStateException expected) {
                        return false;
                    }
                };
            int success = 0;
            for (var result : pool.invokeAll(List.of(a, b))) {
                if (result.get(10, TimeUnit.SECONDS)) {
                    success++;
                }
            }
            assertEquals(1, success);
        } finally {
            pool.shutdownNow();
        }
        assertEquals(1, count("order_after_sale"));
    }

    @Test
    void itemOwnershipRequestBindingAndApprovedRefundBlockAreConsistentAcrossOrderReads()
        throws Exception {
        var first = paidOrder(null);
        var second =
            placement.createDirect(
                new SettlementDirectCmd(
                    1L,
                    List.of(new SettlementDirectCmd.OrderLine(sku, 1)),
                    "ANOTHER",
                    null));
        var service = tradeComponents.get(AfterSaleService.class);
        var foreign = service.items(second.orderNo()).get(0);
        assertThrows(
            IllegalArgumentException.class,
            () ->
                service.create(
                    new AfterSaleCreateCmd(
                        first.orderNo(),
                        "FOREIGN-ITEM",
                        foreign.orderItemId(),
                        1,
                        "REFUND_ONLY",
                        "错误商品")));
        var sale = service.create(itemRefund(first.orderNo(), "BINDING", 1, "REFUND_ONLY"));
        var changed = itemRefund(first.orderNo(), "BINDING", 2, "REFUND_ONLY");
        assertThrows(IllegalStateException.class, () -> service.create(changed));
        service.agree(sale.refundNo());
        var page =
            tradeComponents
                .get(OrderPageQueryExe.class)
                .execute(new OrderPageQry(first.orderNo(), null, 1, 20));
        assertTrue(page.records().get(0).activeRefund());
        assertThrows(
            IllegalStateException.class,
            () -> fulfillmentService.ship(shipment(first.orderNo())));
        assertThrows(
            IllegalStateException.class,
            () -> service.reject(new AfterSaleRejectCmd(sale.refundNo(), "不能倒退")));
    }

    @Test
    void shipmentPagesFilterOnlyRealPersistedWaybillsAndReadDoesNotChangeReceipt()
        throws Exception {
        var order = paidOrder(null);
        fulfillmentService.ship(shipment(order.orderNo()));
        var query = tradeComponents.get(ShipmentPageQueryExe.class);
        // 真实登记的运单按实际值筛选，不使用调用方伪造快递号。
        var stored = shipmentGateway.shipment(order.orderNo());
        var pending =
            query.execute(
                new ShipmentPageQry(
                    order.orderNo(),
                    stored.carrierCode(),
                    stored.trackingNo(),
                    "SHIPPED",
                    1,
                    20));
        assertEquals(1, pending.getTotal());
        assertEquals(
            0,
            query.execute(new ShipmentPageQry(order.orderNo(), null, "OTHER", null, 1, 20))
                .getTotal());
        var detail = tradeComponents.get(ShipmentDetailQueryExe.class).execute(order.orderNo());
        assertEquals(stored.phone(), detail.phone());
        assertNull(detail.receivedAt());
        fulfillmentService.receive(order.orderNo());
        assertEquals(
            1,
            query.execute(new ShipmentPageQry(order.orderNo(), null, null, "RECEIVED", 1, 20))
                .getTotal());
        assertEquals(
            0,
            query.execute(new ShipmentPageQry(order.orderNo(), null, null, "SHIPPED", 1, 20))
                .getTotal());
    }

    @Test
    void partialRefundRoundingRejectionAndFinalCentNeverExceedRealPaidAmount() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, new BigDecimal("10.01")).id());
        var order =
            placement.createDirect(
                new SettlementDirectCmd(
                    1L,
                    List.of(new SettlementDirectCmd.OrderLine(sku, 3)),
                    "ROUNDING",
                    coupon.id()));
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        notify.execute(
            new VerifiedPaymentNotification(
                payment.paymentNo(),
                order.orderNo(),
                "ALIPAY",
                "ROUNDING-CH",
                payment.amount(),
                "ROUNDING-NOTICE"));
        assertEquals(new BigDecimal("19.99"), payment.amount());
        var service = tradeComponents.get(AfterSaleService.class);
        var a = service.create(itemRefund(order.orderNo(), "ROUND-A", 1, "REFUND_ONLY"));
        assertThrows(
            IllegalStateException.class,
            () ->
                service.create(
                    itemRefund(order.orderNo(), "ROUND-BLOCKED", 1, "REFUND_ONLY")));
        service.reject(new AfterSaleRejectCmd(a.refundNo(), "释放额度"));
        successfulRefundChannel();
        var b = service.create(itemRefund(order.orderNo(), "ROUND-B", 1, "REFUND_ONLY"));
        service.agree(b.refundNo());
        tradeComponents.get(PaymentRefundCreateCmdExe.class).execute(b.refundNo());
        var c = service.create(itemRefund(order.orderNo(), "ROUND-C", 1, "REFUND_ONLY"));
        service.agree(c.refundNo());
        tradeComponents.get(PaymentRefundCreateCmdExe.class).execute(c.refundNo());
        var d = service.create(itemRefund(order.orderNo(), "ROUND-D", 1, "REFUND_ONLY"));
        assertEquals(new BigDecimal("19.99"), b.amount().add(c.amount()).add(d.amount()));
        assertEquals(new BigDecimal("6.67"), d.amount());
        service.agree(d.refundNo());
        tradeComponents.get(PaymentRefundCreateCmdExe.class).execute(d.refundNo());
        assertEquals("REFUNDED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals(0, service.items(order.orderNo()).get(0).refundableQuantity());
        assertThrows(
            IllegalStateException.class,
            () ->
                service.create(
                    itemRefund(order.orderNo(), "ROUND-EXCESS", 1, "REFUND_ONLY")));
    }

    @Test
    void orderStatisticsCountOnlySuccessfulReverseTransactionsAndRemarksCanBeCleared()
        throws Exception {
        var order = paidOrder(null);
        var statistics = tradeComponents.get(OrderStatisticsQueryExe.class);
        var initial = statistics.execute(order.orderNo());
        assertEquals(1, initial.orderCount());
        assertEquals(0, initial.afterSaleCount());
        assertEquals(0, initial.afterSalePrice().signum());
        var sale =
            tradeComponents
                .get(AfterSaleService.class)
                .create(itemRefund(order.orderNo(), "STATS-PARTIAL", 1, "REFUND_ONLY"));
        assertEquals(0, statistics.execute(order.orderNo()).afterSaleCount());
        tradeComponents.get(AfterSaleService.class).agree(sale.refundNo());
        successfulRefundChannel();
        tradeComponents.get(PaymentRefundCreateCmdExe.class).execute(sale.refundNo());
        var result = statistics.execute(order.orderNo());
        assertEquals(1, result.afterSaleCount());
        assertEquals(new BigDecimal("10.00"), result.afterSalePrice());
        assertEquals(new BigDecimal("20.00"), result.orderPayPrice());
        var remark = tradeComponents.get(OrderRemarkUpdateCmdExe.class);
        remark.execute(new OrderRemarkUpdateCmd(order.orderNo(), "联系买家"));
        assertEquals("联系买家", tradeOrderGateway.remark(order.orderNo()));
        remark.execute(new OrderRemarkUpdateCmd(order.orderNo(), ""));
        assertEquals("", tradeOrderGateway.remark(order.orderNo()));
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void newAfterSaleRejectionAlsoReleasesLegacyWholeOrderUniquePlaceholder() throws Exception {
        var order = paidOrder(null);
        var original = requestAfterSale(new RefundRequestCmd(order.orderNo(), "OLD-1", "旧整单售后"));
        tradeComponents
            .get(AfterSaleService.class)
            .reject(new AfterSaleRejectCmd(original.refundNo(), "拒绝"));
        var next = requestAfterSale(new RefundRequestCmd(order.orderNo(), "OLD-2", "再次申请"));
        assertNotEquals(original.refundNo(), next.refundNo());
        assertEquals("REQUESTED", next.status());
    }

    void useCatalogSkuForReview() {
        var category =
            mallComponents
                .get(ProductCategoryGateway.class)
                .saveCategory(new ProductCategory(null, null, "评价分类", null, 0, "ENABLED"));
        var brand =
            mallComponents
                .get(ProductBrandGateway.class)
                .saveBrand(new ProductBrand(null, "评价品牌", null, null, 0, "ENABLED"));
        var product =
            mallComponents
                .get(ProductCatalogService.class)
                .createSpu(
                    ProductCatalogValidation.newSpu(
                        new ProductSpu(
                            null,
                            "COMMENT-SPU",
                            "评价商品",
                            category.id(),
                            brand.id(),
                            null,
                            null,
                            null,
                            List.of(
                                new ProductSku(
                                    null,
                                    null,
                                    "COMMENT-SKU",
                                    "评价SKU",
                                    BigDecimal.TEN,
                                    10,
                                    null,
                                    Map.of(),
                                    null)))));
        mallComponents.get(ProductSpuGateway.class).shelf(product.id(), true);
        sku = product.skus().get(0).id();
        cartService.saveCart(1L, sku, 2, true);
    }

    /** 投稿入口迁至order后，评价写路径的测试门面：create转order核验入口并读回，reply委托product。 */
    private ReviewFacade reviewFacade() {
        return new ReviewFacade(
            tradeComponents.get(OrderCommentService.class),
            mallComponents.get(ProductCommentService.class),
            mallComponents.get(ProductCommentGateway.class));
    }

    static final class ReviewFacade {
        private final OrderCommentService submissions;
        private final ProductCommentService writes;
        private final ProductCommentGateway rows;

        ReviewFacade(OrderCommentService submissions, ProductCommentService writes, ProductCommentGateway rows) {
            this.submissions = submissions;
            this.writes = writes;
            this.rows = rows;
        }

        ProductComment create(ProductCommentCreateCmd cmd) {
            var id =
                submissions.create(
                    new OrderCommentCreateCmd(
                        cmd.memberId(),
                        cmd.orderNo(),
                        cmd.skuId(),
                        cmd.orderItemId(),
                        cmd.score(),
                        cmd.content(),
                        cmd.descriptionScores(),
                        cmd.benefitScores(),
                        cmd.anonymous(),
                        cmd.picUrls(),
                        null,
                        null));
            return rows.find(id);
        }

        ProductComment reply(ProductCommentReplyCmd cmd) {
            return writes.reply(cmd);
        }
    }

    @Test
    void commentsRequireActualOwnedCompletedOrderAndCannotForgeBuyerOrProduct() throws Exception {
        useCatalogSkuForReview();
        var order = paidOrder(null);
        var comments = reviewFacade();
        var cmd = new ProductCommentCreateCmd(1L, order.orderNo(), sku, 5, "真实评价");
        assertThrows(ServiceException.class, () -> comments.create(cmd));
        fulfillmentService.ship(shipment(order.orderNo()));
        fulfillmentService.receive(order.orderNo());
        assertThrows(
            ServiceException.class,
            () -> comments.create(new ProductCommentCreateCmd(2L, order.orderNo(), sku, 5, "他人订单")));
        assertThrows(
            ServiceException.class,
            () ->
                comments.create(
                    new ProductCommentCreateCmd(1L, order.orderNo(), sku + 999, 5, "错误商品")));
        var saved = comments.create(cmd);
        assertEquals(1L, saved.memberId());
        assertEquals(saved, comments.create(cmd));
        assertThrows(
            ServiceException.class,
            () -> comments.create(new ProductCommentCreateCmd(1L, order.orderNo(), sku, 1, "更改原评价")));
        assertThrows(
            ServiceException.class,
            () -> comments.create(new ProductCommentCreateCmd(1L, order.orderNo(), sku, 6, "错误评分")));
    }

    @Test
    void merchantReplyAndVisibilityKeepBuyerRatingAndFilteringUsesRealCommentRows()
        throws Exception {
        useCatalogSkuForReview();
        var order = paidOrder(null);
        fulfillmentService.ship(shipment(order.orderNo()));
        fulfillmentService.receive(order.orderNo());
        var comments = reviewFacade();
        var saved = comments.create(new ProductCommentCreateCmd(1L, order.orderNo(), sku, 5, "已完成订单评价"));
        var replied = comments.reply(new ProductCommentReplyCmd(saved.id(), 99L, "感谢反馈"));
        assertEquals(99L, replied.replyUserId());
        assertEquals(replied, comments.reply(new ProductCommentReplyCmd(saved.id(), 99L, "感谢反馈")));
        mallComponents
            .get(ProductCommentVisibleCmdExe.class)
            .execute(new ProductCommentVisibleCmd(saved.id(), false));
        var query = mallComponents.get(ProductCommentPageQueryExe.class);
        var page = query.execute(new ProductCommentPageQry(saved.spuId(), 1L, 5, false, true, 1, 20));
        assertEquals(1, page.getTotal());
        assertEquals("已完成订单评价", page.getRecords().get(0).content());
        assertEquals("感谢反馈", page.getRecords().get(0).replyContent());
        assertEquals(
            0,
            query.execute(new ProductCommentPageQry(saved.spuId(), 1L, 5, true, null, 1, 20))
                .getTotal());
    }

    @Test
    void concurrentReviewSubmissionsReturnOneStableIdForTheSameOrderSku() throws Exception {
        useCatalogSkuForReview();
        var order = paidOrder(null);
        fulfillmentService.ship(shipment(order.orderNo()));
        fulfillmentService.receive(order.orderNo());
        var service = reviewFacade();
        var cmd = new ProductCommentCreateCmd(1L, order.orderNo(), sku, 4, "重复提交只保存一次");
        var pool = Executors.newFixedThreadPool(2);
        try {
            var a = pool.submit(() -> service.create(cmd));
            var b = pool.submit(() -> service.create(cmd));
            assertEquals(a.get(10, TimeUnit.SECONDS).id(), b.get(10, TimeUnit.SECONDS).id());
        } finally {
            pool.shutdownNow();
        }
        assertNotNull(
            mallComponents
                .get(ProductCommentGateway.class)
                .findByOrderItem(
                    orderRpc.findByOrderNo(order.orderNo()).items().stream()
                        .filter(item -> item.productId().equals(sku))
                        .findFirst()
                        .orElseThrow()
                        .orderItemId()));
    }

    @Test
    void deletedCatalogStillRetainsCompletedPurchaseAndCommentHistory() throws Exception {
        useCatalogSkuForReview();
        var order = paidOrder(null);
        fulfillmentService.ship(shipment(order.orderNo()));
        fulfillmentService.receive(order.orderNo());
        var parentId = productSkuGateway.catalogSku(sku).spuId();
        var management = mallComponents.get(ProductManagementService.class);
        mallComponents.get(ProductSpuGateway.class).shelf(parentId, false);
        management.recycle(parentId);
        management.delete(parentId);
        var saved =
            reviewFacade()
                .create(new ProductCommentCreateCmd(1L, order.orderNo(), sku, 5, "历史订单仍可反馈"));
        assertEquals(parentId, saved.spuId());
        assertEquals("DELETED", productSkuGateway.catalogSku(sku).status());
        assertEquals("COMPLETED", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    /**
     * 旧用例的数据准备改走商品售后，以第一个商品的全部数量申请。
     */
    private RefundRequest requestAfterSale(RefundRequestCmd cmd) {
        var order = orderPersistenceService.findByOrderNo(cmd.orderNo());
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        var line = tradeComponents.get(TradeOrderItemGateway.class).refundItems(order.id()).get(0);
        var sale =
            tradeComponents
                .get(AfterSaleService.class)
                .create(
                    new AfterSaleCreateCmd(
                        cmd.orderNo(),
                        cmd.requestNo(),
                        line.id(),
                        line.quantity(),
                        "REFUND_ONLY",
                        cmd.reason()));
        return legacyView(sale);
    }

    private RefundRequest rejectAfterSale(RefundRejectCmd cmd) {
        return legacyView(
            tradeComponents
                .get(AfterSaleService.class)
                .reject(new AfterSaleRejectCmd(cmd.refundNo(), cmd.reason())));
    }

    private RefundRequest legacyView(AfterSale sale) {
        return new RefundRequest(
            sale.refundNo(),
            sale.orderNo(),
            sale.requestNo(),
            sale.memberId(),
            sale.paymentNo(),
            sale.amount(),
            sale.reason(),
            sale.status(),
            sale.decisionReason(),
            sale.requestedAt(),
            sale.decidedAt());
    }

    private RefundRequest approvedAfterSale(RefundRequestCmd cmd) {
        var requested = requestAfterSale(cmd);
        return legacyView(tradeComponents.get(AfterSaleService.class).agree(requested.refundNo()));
    }

    /**
     * 真正业务状态变化才生成日志，重放不重复记录。
     */
    @Test
    void orderLogsTrackCreatePayShipAndMemberReceiveExactlyOnce() {
        var order = paidOrder(null);
        var command = shipment(order.orderNo());
        var operator = TradeOperator.administrator(55L);
        var shipping =
            new ShipmentCreateCmd(
                command.orderNo(),
                command.requestNo(),
                command.carrierCode(),
                command.trackingNo(),
                command.recipientName(),
                command.phone(),
                command.address(),
                operator);
        fulfillmentService.ship(shipping);
        fulfillmentService.ship(shipping);
        assertThrows(
            ServiceException.class,
            () -> fulfillmentService.receive(order.orderNo(), TradeOperator.member(2L)));
        fulfillmentService.receive(order.orderNo(), TradeOperator.member(1L));
        fulfillmentService.receive(order.orderNo(), TradeOperator.member(1L));
        var header = orderPersistenceService.findByOrderNo(order.orderNo());
        var logs = tradeComponents.get(OrderLogGateway.class).byOrderId(header.id());
        assertEquals(List.of(1, 10, 20, 30), logs.stream().map(OrderLog::operateType).toList());
        assertEquals(List.of(0, 10, 20, 30), logs.stream().map(OrderLog::afterStatus).toList());
        assertEquals(55L, logs.get(2).userId());
        assertEquals(2, logs.get(2).userType());
        assertEquals(1L, logs.get(3).userId());
        assertNotNull(logs.get(3).createTime());
    }

    /**
     * 会员取消只允许本人，库存与券的补偿仍在提交后异步发送。
     */
    @Test
    void memberCancelChecksOwnerAndRepeatedCancellationDoesNotDuplicateLog() {
        var order = place("CANCEL-LOG", null);
        assertThrows(ServiceException.class, () -> closer.executeForMember(2L, order.orderNo()));
        assertTrue(closer.executeForMember(1L, order.orderNo()));
        assertTrue(closer.executeForMember(1L, order.orderNo()));
        var header = orderPersistenceService.findByOrderNo(order.orderNo());
        var logs = tradeComponents.get(OrderLogGateway.class).byOrderId(header.id());
        assertEquals(List.of(1, 40), logs.stream().map(OrderLog::operateType).toList());
        assertEquals(1L, logs.get(1).userId());
        assertEquals("CLOSED", header.status());
    }

    /**
     * 订单日志失败回滚订单本地事务，真实支付与原资金事件保留，重试仅记一次日志。
     */
    @Test
    void paymentLogFailureRollsBackStateAndRetryProducesSinglePaymentLog() throws Exception {
        var order = place("LOG-FAILURE", null);
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        sql(
            tradeSource,
            "ALTER TABLE order_log ADD CONSTRAINT reject_payment_log CHECK(operate_type<>10)");
        var notification =
            new VerifiedPaymentNotification(
                payment.paymentNo(),
                order.orderNo(),
                "ALIPAY",
                "LOG-PAY",
                order.totalAmount(),
                "NOTICE-LOG");
        assertThrows(RuntimeException.class, () -> notify.execute(notification));
        assertEquals(
            "PENDING_PAYMENT", orderPersistenceService.findByOrderNo(order.orderNo()).status());
        assertEquals("SUCCESS", payments.findByPaymentNo(payment.paymentNo()).status());
        sql(tradeSource, "ALTER TABLE order_log DROP CONSTRAINT reject_payment_log");
        events.drainOrThrow();
        notify.execute(notification);
        notify.execute(notification);
        var header = orderPersistenceService.findByOrderNo(order.orderNo());
        var logs = tradeComponents.get(OrderLogGateway.class).byOrderId(header.id());
        assertEquals(List.of(1, 10), logs.stream().map(OrderLog::operateType).toList());
    }

    /**
     * 退货退款各阶段按实际操作者记录源状态，回调重放不重复日志。
     */
    @Test
    void afterSaleReturnRefundLogsUseNumericIdentityAndRecordRealSuccessTime() {
        var order = paidOrder(null);
        fulfillmentService.ship(shipment(order.orderNo()));
        var service = tradeComponents.get(AfterSaleService.class);
        var member = TradeOperator.member(1L);
        var admin = TradeOperator.administrator(55L);
        var sale =
            service.create(
                itemRefund(order.orderNo(), "LOG-RETURN", 1, "RETURN_REFUND"), member);
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
        var logs = tradeComponents.get(AfterSaleLogGateway.class).byAfterSaleId(sale.id());
        assertEquals(
            List.of(10, 11, 20, 21, 30, 31),
            logs.stream().map(AfterSaleLog::operateType).toList());
        assertEquals(
            List.of(10, 20, 30, 40, 40, 50),
            logs.stream().map(AfterSaleLog::afterStatus).toList());
        assertEquals(55L, logs.get(1).userId());
        assertEquals(2, logs.get(1).userType());
        assertEquals(1L, logs.get(2).userId());
        assertEquals(0L, logs.get(5).userId());
        assertNotNull(refundRequestGateway.afterSale(sale.refundNo()).refundTime());
        assertEquals(55L, refundRequestGateway.afterSale(sale.refundNo()).auditUserId());
    }

    /**
     * 会员取消必须先校验归属，重放也不能查看他人的记录。
     */
    @Test
    void memberAfterSaleCancelRestoresQuotaAndCannotCancelRefundStage() {
        var order = paidOrder(null);
        var service = tradeComponents.get(AfterSaleService.class);
        var sale = service.create(itemRefund(order.orderNo(), "CANCEL-SALE", 1, "REFUND_ONLY"));
        assertThrows(ServiceException.class, () -> service.cancel(2L, sale.refundNo()));
        service.cancel(1L, sale.refundNo());
        service.cancel(1L, sale.refundNo());
        assertEquals(2, service.items(order.orderNo()).get(0).refundableQuantity());
        var logs = tradeComponents.get(AfterSaleLogGateway.class).byAfterSaleId(sale.id());
        assertEquals(List.of(10, 40), logs.stream().map(AfterSaleLog::operateType).toList());
        assertEquals(61, logs.get(1).afterStatus());
        assertThrows(ServiceException.class, () -> service.cancel(2L, sale.refundNo()));
        var next = service.create(itemRefund(order.orderNo(), "AFTER-CANCEL", 1, "REFUND_ONLY"));
        service.agree(next.refundNo());
        assertThrows(ServiceException.class, () -> service.cancel(1L, next.refundNo()));
        assertEquals("WAIT_REFUND", refundRequestGateway.afterSale(next.refundNo()).status());
        assertThrows(ServiceException.class, () -> service.cancel(1L, "MISSING-SALE"));
    }

    /**
     * 日志失败不能提交审批；真实失败回调只记录一次且不伪造退款时间。
     */
    @Test
    void afterSaleLogFailureRollsBackApprovalAndFailedRefundReplayDoesNotDuplicateLog()
        throws Exception {
        var order = paidOrder(null);
        var service = tradeComponents.get(AfterSaleService.class);
        var sale = service.create(itemRefund(order.orderNo(), "LOG-FAIL-SALE", 1, "REFUND_ONLY"));
        sql(
            tradeSource,
            "ALTER TABLE order_after_sale_log ADD CONSTRAINT reject_agreement_log CHECK(operate_type<>11)");
        assertThrows(RuntimeException.class, () -> service.agree(sale.refundNo()));
        assertEquals("REQUESTED", refundRequestGateway.afterSale(sale.refundNo()).status());
        sql(tradeSource, "ALTER TABLE order_after_sale_log DROP CONSTRAINT reject_agreement_log");
        service.agree(sale.refundNo());
        Mockito.when(refundClient.refund(
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(
                call -> {
                    PaymentRefund refund = call.getArgument(0);
                    return new PaymentRefundResult(
                        refund.refundNo(),
                        refund.paymentNo(),
                        refund.channel(),
                        refund.amount(),
                        "FAILED",
                        null);
                });
        var create = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        sql(
            tradeSource,
            "ALTER TABLE order_after_sale_log ADD CONSTRAINT reject_refund_failure_log CHECK(operate_type<>32)");
        assertThrows(RuntimeException.class, () -> create.execute(sale.refundNo()));
        assertEquals(
            "FAILED",
            tradeComponents
                .get(PaymentRefundGateway.class)
                .findByAfterSaleNo(sale.refundNo())
                .status());
        assertEquals("REFUNDING", refundRequestGateway.afterSale(sale.refundNo()).status());
        sql(
            tradeSource,
            "ALTER TABLE order_after_sale_log DROP CONSTRAINT reject_refund_failure_log");
        create.execute(sale.refundNo());
        create.execute(sale.refundNo());
        var logs = tradeComponents.get(AfterSaleLogGateway.class).byAfterSaleId(sale.id());
        assertEquals(
            List.of(10, 11, 30, 32), logs.stream().map(AfterSaleLog::operateType).toList());
        assertNull(refundRequestGateway.afterSale(sale.refundNo()).refundTime());
        assertEquals("PAID", orderPersistenceService.findByOrderNo(order.orderNo()).status());
    }

    /**
     * 实际商品、结算、订单落库及售后共用下单时快照，商品修改不改历史。
     */
    @Test
    void originalProductSnapshotSurvivesCatalogChangesAndAfterSalePersistence() throws Exception {
        useCatalogSkuForReview();
        var spuId = productSkuGateway.catalogSku(sku).spuId();
        sql(
            mallSource,
            "UPDATE product_sku SET main_image='https://example.com/old.png' WHERE id=" + sku);
        var dict = mallComponents.get(ProductSpecService.class);
        var spec = dict.create(new ProductSpecCreateCmd("颜色", null));
        var option = dict.createValue(new ProductSpecValueCreateCmd(spec.id(), "黑色", null));
        var catalog = mallComponents.get(ProductCatalogService.class);
        var originalSpu = catalog.spu(spuId);
        var originalSku = originalSpu.skus().get(0);
        var linked =
            new ProductSku(
                originalSku.id(),
                spuId,
                originalSku.code(),
                originalSku.name(),
                originalSku.price(),
                originalSku.stock(),
                originalSku.image(),
                List.of(new ProductSkuSpec(spec.id(), null, option.id(), null)),
                originalSku.status(),
                originalSku.weight(),
                originalSku.volume());
        catalog.updateSpu(
            new ProductSpu(
                spuId,
                originalSpu.code(),
                originalSpu.name(),
                originalSpu.categoryId(),
                originalSpu.brandId(),
                originalSpu.description(),
                originalSpu.image(),
                originalSpu.status(),
                List.of(linked),
                originalSpu.deliveryTypes(),
                originalSpu.deliveryTemplateId(),
                originalSpu.keyword(),
                originalSpu.introduction(),
                originalSpu.sliderPicUrls(),
                originalSpu.sort()));
        var receipt = paidOrder(null);
        var order = orderPersistenceService.findByOrderNo(receipt.orderNo());
        var item = order.items().get(0);
        assertEquals(spuId, item.spuId());
        assertEquals("评价商品", item.spuName());
        assertEquals("颜色", item.specs().get(0).specName());
        assertEquals("黑色", item.specs().get(0).specValueName());
        assertEquals(spec.id(), item.specs().get(0).specId());
        assertEquals(option.id(), item.specs().get(0).specValueId());
        assertEquals("https://example.com/old.png", item.picUrl());
        var changedItem =
            new TradeOrderItem(
                item.productId(),
                item.productCode(),
                item.productName(),
                item.unitPrice(),
                item.quantity(),
                item.lineAmount(),
                item.payAmount(),
                item.deliveryPrice(),
                item.spuId(),
                item.spuName(),
                item.specs(),
                "https://example.com/forged.png");
        var changedOrder =
            new TradeOrder(
                order.id(),
                order.orderNo(),
                order.memberId(),
                order.totalAmount(),
                order.status(),
                List.of(changedItem),
                order.createTime(),
                order.usePoint(),
                order.givePoint(),
                order.delivery());
        assertThrows(
            IllegalStateException.class,
            () -> tradeComponents.get(OrderSubmissionService.class).create(changedOrder));
        sql(mallSource, "UPDATE product_spu SET name='已改商品' WHERE id=" + spuId);
        sql(
            mallSource,
            "UPDATE product_sku SET main_image='https://example.com/new.png' WHERE id=" + sku);
        dict.update(new ProductSpecUpdateCmd(spec.id(), "色彩", null));
        dict.updateValue(new ProductSpecValueUpdateCmd(option.id(), "白色", null));
        assertEquals("白色", catalog.sku(sku).specs().get(0).specValueName());
        assertEquals(item, orderPersistenceService.findByOrderNo(order.orderNo()).items().get(0));
        var service = tradeComponents.get(AfterSaleService.class);
        var sale = service.create(itemRefund(order.orderNo(), "SNAPSHOT", 1, "REFUND_ONLY"));
        var saved = refundRequestGateway.afterSale(sale.refundNo());
        assertEquals(sku, saved.skuId());
        assertEquals(item.spuId(), saved.spuId());
        assertEquals(item.spuName(), saved.spuName());
        assertEquals(item.specs().get(0).specName(), saved.specs().get(0).specName());
        assertEquals(item.specs().get(0).specValueName(), saved.specs().get(0).specValueName());
        assertEquals(item.specs().get(0).specId(), saved.specs().get(0).specId());
        assertEquals(item.specs().get(0).specValueId(), saved.specs().get(0).specValueId());
        assertEquals(item.picUrl(), saved.picUrl());
        service.agree(saved.refundNo());
        assertEquals(saved.specs(), refundRequestGateway.afterSale(saved.refundNo()).specs());
    }

    /**
     * 报价后规格改变必须重新确认，不能静默提交新规格。
     */
    @Test
    void changedSpecsInvalidateQuoteBeforeAnyOrderIsWritten() throws Exception {
        useCatalogSkuForReview();
        var token =
            SettlementRules.quoteToken(
                placement.quote(new SettlementQuoteQry(1L, List.of(sku), null)));
        skuProperties(List.of(new ProductSkuSpec(null, "颜色", null, "新规格")));
        var failure =
            assertThrows(
                IllegalStateException.class,
                () ->
                    placement.checkoutCart(
                        new SettlementOrderSubmitCmd(
                            1L, "CHANGED-SPEC", List.of(sku), null, token)));
        assertEquals("价格或数量已变化，请重新预览并确认", failure.getMessage());
        assertEquals(0, count("order_info"));
    }

    /**
     * 字典关联从旧未知变为已知，名称和值未变也必须重新确认报价。
     */
    @Test
    void changingOnlySpecIdsRejectsOldQuoteAndCreatesNoOrder() throws Exception {
        useCatalogSkuForReview();
        skuProperties(List.of(new ProductSkuSpec(null, "颜色", null, "黑色")));
        var token =
            SettlementRules.quoteToken(
                placement.quote(new SettlementQuoteQry(1L, List.of(sku), null)));
        var dict = mallComponents.get(ProductSpecService.class);
        var spec = dict.create(new ProductSpecCreateCmd("颜色", null));
        var option = dict.createValue(new ProductSpecValueCreateCmd(spec.id(), "黑色", null));
        skuProperties(List.of(new ProductSkuSpec(spec.id(), "颜色", option.id(), "黑色")));
        assertThrows(
            IllegalStateException.class,
            () ->
                placement.checkoutCart(
                    new SettlementOrderSubmitCmd(
                        1L, "CHANGED-SPEC-ID", List.of(sku), null, token)));
        assertEquals(0, count("order_info"));
    }

    private void skuProperties(List<ProductSkuSpec> specs) {
        mallComponents
            .get(ProductSkuMapper.class)
            .update(
                null,
                Wrappers.lambdaUpdate(ProductSkuDO.class)
                    .eq(ProductSkuDO::getId, sku)
                    .set(ProductSkuDO::getSpecs, JSON.toJSONString(specs)));
    }

    @Test
    void commentsUseRealItemIdsAndOnlyAllItemsFinishTheOrderAndLogOnce() throws Exception {
        var order = completedReviewOrderWithTwoItems();
        assertEquals(2, order.items().size());
        assertFalse(order.commentStatus());
        assertTrue(
            order.items().stream()
                .allMatch(
                    line ->
                        line.orderItemId() != null
                            && Boolean.FALSE.equals(line.commentStatus())));
        var comments = reviewFacade();
        var a = order.items().get(0);
        var b = order.items().get(1);
        var first =
            new ProductCommentCreateCmd(
                1L,
                order.orderNo(),
                a.productId(),
                null,
                "第一项",
                5,
                4,
                false,
                List.of(),
                a.orderItemId());
        var saved = comments.create(first);
        assertEquals(a.orderItemId(), saved.orderItemId());
        assertFalse(orderRpc.findByOrderNo(order.orderNo()).commentStatus());
        assertEquals(
            1,
            orderRpc.findByOrderNo(order.orderNo()).items().stream()
                .filter(line -> Boolean.TRUE.equals(line.commentStatus()))
                .count());
        assertEquals(
            404,
            assertThrows(
                ServiceException.class,
                () ->
                    comments.create(
                        new ProductCommentCreateCmd(
                            1L,
                            order.orderNo(),
                            a.productId(),
                            5,
                            "伪造行",
                            null,
                            null,
                            false,
                            List.of(),
                            b.orderItemId())))
                .getCode());
        comments.create(
            new ProductCommentCreateCmd(
                1L,
                order.orderNo(),
                b.productId(),
                5,
                "第二项",
                null,
                null,
                false,
                List.of(),
                b.orderItemId()));
        var finished = orderRpc.findByOrderNo(order.orderNo());
        assertTrue(finished.commentStatus());
        assertNotNull(finished.finishTime());
        comments.create(first);
        assertEquals(finished.finishTime(), orderRpc.findByOrderNo(order.orderNo()).finishTime());
        var logs = tradeComponents.get(OrderLogGateway.class).byOrderId(order.id());
        assertEquals(1, logs.stream().filter(log -> log.operateType() == 33).count());
        // 只去掉数据库ID及后续评价元数据，保留全部原始购买优惠和权益快照。
        var original = orderPersistenceService.findByOrderNo(order.orderNo());
        tradeComponents
            .get(OrderSubmissionService.class)
            .requireSame(
                original,
                new TradeOrder(
                    null,
                    original.orderNo(),
                    original.memberId(),
                    original.totalAmount(),
                    "RESERVING",
                    original.items().stream()
                        .map(
                            line ->
                                new TradeOrderItem(
                                    line.productId(),
                                    line.productCode(),
                                    line.productName(),
                                    line.unitPrice(),
                                    line.quantity(),
                                    line.lineAmount(),
                                    line.payAmount(),
                                    line.deliveryPrice(),
                                    line.spuId(),
                                    line.spuName(),
                                    line.specs(),
                                    line.picUrl(),
                                    null,
                                    null,
                                    line.discountAmount(),
                                    line.deliveryOriginalPrice(),
                                    line.deliveryDiscountPrice(),
                                    line.adjustmentAmount(),
                                    line.usePoint(),
                                    line.givePoint(),
                                    0))
                        .toList(),
                    null,
                    original.usePoint(),
                    original.givePoint(),
                    original.delivery(),
                    null,
                    null,
                    original.giveCouponTemplateCounts(),
                    original.originalAmount(),
                    original.discountAmount(),
                    original.deliveryOriginalPrice(),
                    original.deliveryDiscountPrice(),
                    original.adjustmentAmount(),
                    original.pricingSnapshotStatus(),
                    original.pricingSnapshotVersion(),
                    original.promotions()));
        var detail = tradeComponents.get(OrderDetailQueryExe.class).execute(order.orderNo());
        assertTrue(detail.commentStatus());
        assertEquals(finished.finishTime(), detail.finishTime());
        assertEquals(a.orderItemId(), detail.items().get(0).orderItemId());
    }

    @Test
    void committedRemoteCommentWithLostResponseIsRepairedByOriginalRetry() throws Exception {
        useCatalogSkuForReview();
        var order = paidOrder(null);
        fulfillmentService.ship(shipment(order.orderNo()));
        fulfillmentService.receive(order.orderNo());
        var line = orderRpc.findByOrderNo(order.orderNo()).items().get(0);
        var cmd =
            new ProductCommentCreateCmd(
                1L,
                order.orderNo(),
                line.productId(),
                null,
                "丢响应",
                5,
                4,
                true,
                List.of(),
                line.orderItemId());
        loseNextCommentResponse = true;
        assertThrows(
            IllegalStateException.class,
            () -> reviewFacade().create(cmd));
        var stored =
            mallComponents.get(ProductCommentGateway.class).findByOrderItem(line.orderItemId());
        assertNotNull(stored);
        assertFalse(orderRpc.findByOrderNo(order.orderNo()).items().get(0).commentStatus());
        var repaired = reviewFacade().create(cmd);
        assertEquals(stored.id(), repaired.id());
        assertTrue(orderRpc.findByOrderNo(order.orderNo()).commentStatus());
        assertEquals(
            1,
            tradeComponents.get(OrderLogGateway.class).byOrderId(order.id()).stream()
                .filter(log -> log.operateType() == 33)
                .count());
        assertEquals(1, count("product_comment"));
    }

    @Test
    void localCommentStateFailureRollsBackFlagsAndLogButOriginalRetryKeepsRemoteId()
        throws Exception {
        useCatalogSkuForReview();
        var order = paidOrder(null);
        fulfillmentService.ship(shipment(order.orderNo()));
        fulfillmentService.receive(order.orderNo());
        var line = orderRpc.findByOrderNo(order.orderNo()).items().get(0);
        var cmd =
            new ProductCommentCreateCmd(
                1L,
                order.orderNo(),
                line.productId(),
                5,
                "事务修复",
                null,
                null,
                false,
                List.of(),
                line.orderItemId());
        sql(
            tradeSource,
            "ALTER TABLE order_info ADD CONSTRAINT reject_review_done CHECK(comment_status IS NULL OR comment_status=FALSE)");
        assertThrows(
            RuntimeException.class,
            () -> reviewFacade().create(cmd));
        var saved =
            mallComponents.get(ProductCommentGateway.class).findByOrderItem(line.orderItemId());
        assertNotNull(saved);
        assertFalse(orderRpc.findByOrderNo(order.orderNo()).commentStatus());
        assertFalse(orderRpc.findByOrderNo(order.orderNo()).items().get(0).commentStatus());
        assertEquals(
            0,
            tradeComponents.get(OrderLogGateway.class).byOrderId(order.id()).stream()
                .filter(log -> log.operateType() == 33)
                .count());
        sql(tradeSource, "ALTER TABLE order_info DROP CONSTRAINT reject_review_done");
        assertEquals(saved.id(), reviewFacade().create(cmd).id());
        var finished = orderRpc.findByOrderNo(order.orderNo());
        assertTrue(finished.commentStatus());
        assertNotNull(finished.finishTime());
        assertEquals(1, count("product_comment"));
    }

    private SettlementOrder completedReviewOrderWithTwoItems() {
        useCatalogSkuForReview();
        var firstSku = sku;
        var catalog = mallComponents.get(ProductCatalogService.class);
        var spuGateway = mallComponents.get(ProductSpuGateway.class);
        var firstSpu = spuGateway.findSpu(productSkuGateway.catalogSku(firstSku).spuId());
        var second =
            catalog.createSpu(
                ProductCatalogValidation.newSpu(
                    new ProductSpu(
                        null,
                        "COMMENT-TWO",
                        "第二评价商品",
                        firstSpu.categoryId(),
                        firstSpu.brandId(),
                        null,
                        null,
                        null,
                        List.of(
                            new ProductSku(
                                null,
                                null,
                                "COMMENT-SKU-TWO",
                                "第二规格",
                                BigDecimal.TEN,
                                10,
                                null,
                                Map.of(),
                                null)))));
        spuGateway.shelf(second.id(), true);
        Long secondSku = second.skus().get(0).id();
        cartService.saveCart(1L, secondSku, 1, true);
        var ids = List.of(firstSku, secondSku);
        var quote = placement.quote(new SettlementQuoteQry(1L, ids, null));
        var created =
            placement.checkoutCart(
                new SettlementOrderSubmitCmd(
                    1L, "REVIEW-TWO", ids, null, SettlementRules.quoteToken(quote)));
        worker().execute(50);
        var payment = paymentCreate.execute(created.orderNo(), "ALIPAY");
        notify.execute(
            new VerifiedPaymentNotification(
                payment.paymentNo(),
                created.orderNo(),
                "ALIPAY",
                "CH-C",
                created.totalAmount(),
                "N-C"));
        fulfillmentService.ship(shipment(created.orderNo()));
        fulfillmentService.receive(created.orderNo());
        return orderRpc.findByOrderNo(created.orderNo());
    }

    @Test
    void concurrentDifferentItemReviewsConvergeAndCreateOneCompletionLog() throws Exception {
        var order = completedReviewOrderWithTwoItems();
        var service = reviewFacade();
        var pool = Executors.newFixedThreadPool(2);
        try {
            var requests =
                order.items().stream()
                    .map(
                        line ->
                            pool.submit(
                                () ->
                                    service.create(
                                        new ProductCommentCreateCmd(
                                            1L,
                                            order.orderNo(),
                                            line.productId(),
                                            null,
                                            "并发评价",
                                            5,
                                            4,
                                            false,
                                            List.of(),
                                            line.orderItemId()))))
                    .toList();
            var ids = new HashSet<Long>();
            for (var request : requests) ids.add(request.get(10, TimeUnit.SECONDS).id());
            assertEquals(2, ids.size());
        } finally {
            pool.shutdownNow();
        }
        assertTrue(orderRpc.findByOrderNo(order.orderNo()).commentStatus());
        assertEquals(2, count("product_comment"));
        assertEquals(
            1,
            tradeComponents.get(OrderLogGateway.class).byOrderId(order.id()).stream()
                .filter(log -> log.operateType() == 33)
                .count());
    }

    @Test
    void historicalUnknownFlagsAreNotFakedFalseAndKnownLegacyReviewBindsRealItem()
        throws Exception {
        useCatalogSkuForReview();
        var order = paidOrder(null);
        fulfillmentService.ship(shipment(order.orderNo()));
        fulfillmentService.receive(order.orderNo());
        var actual = orderRpc.findByOrderNo(order.orderNo());
        var line = actual.items().get(0);
        sql(tradeSource, "UPDATE order_info SET comment_status=NULL WHERE id=" + order.id());
        sql(tradeSource, "UPDATE order_item SET comment_status=NULL WHERE order_id=" + order.id());
        assertNull(orderRpc.findByOrderNo(order.orderNo()).commentStatus());
        assertNull(orderRpc.findByOrderNo(order.orderNo()).items().get(0).commentStatus());
        var old =
            mallComponents
                .get(ProductCommentGateway.class)
                .save(
                    new ProductComment(
                        null,
                        productSkuGateway.catalogSku(sku).spuId(),
                        sku,
                        order.orderNo(),
                        1L,
                        5,
                        "旧购买评价",
                        true,
                        null,
                        null,
                        null,
                        null));
        var repaired =
            reviewFacade()
                .create(new ProductCommentCreateCmd(1L, order.orderNo(), sku, 5, "旧购买评价"));
        assertEquals(old.id(), repaired.id());
        assertEquals(line.orderItemId(), repaired.orderItemId());
        assertEquals(order.id(), repaired.orderId());
        assertNull(repaired.spuName());
        assertNull(repaired.descriptionScores());
        assertNull(repaired.userNickname());
        assertTrue(orderRpc.findByOrderNo(order.orderNo()).commentStatus());
    }

    @Test
    void submittedItemIdsAndReviewFlagsCannotForgeDatabaseGeneratedMetadata() throws Exception {
        useCatalogSkuForReview();
        var parent = productSkuGateway.catalogSku(sku).spuId();
        var fakeLine =
            new TradeOrderItem(
                sku,
                "COMMENT-SKU",
                "购买商品",
                BigDecimal.TEN,
                1,
                BigDecimal.TEN,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                parent,
                "购买商品",
                List.of(),
                null,
                999999L,
                true);
        var fake =
            new TradeOrder(
                null,
                "META-FORGE",
                1L,
                BigDecimal.TEN,
                "RESERVING",
                List.of(fakeLine),
                null,
                0L,
                0L,
                null,
                true,
                LocalDateTime.of(2000, 1, 1, 0, 0));
        // 直接验证交易持久化边界，避免绕过结算回执校验构造无效 RPC 下单请求。
        tradeComponents.get(OrderSubmissionService.class).create(fake);
        var saved = orderRpc.findByOrderNo(fake.orderNo());
        assertNotEquals(999999L, saved.items().get(0).orderItemId());
        assertNotNull(saved.items().get(0).orderItemId());
        assertFalse(saved.items().get(0).commentStatus());
        assertFalse(saved.commentStatus());
        assertNull(saved.finishTime());
    }

    @Test
    void systemCommentRequiresExpiredReceiptAndKeepsOneCompletionLog() {
        var order = completedReviewOrderWithTwoItems();
        var service = tradeComponents.get(OrderCommentService.class);
        var time = tradeOrderGateway.databaseNow();
        assertFalse(service.createSystem(order.orderNo(), time.minusDays(7)));
        assertTrue(service.createSystem(order.orderNo(), time.plusDays(1)));
        assertFalse(service.createSystem(order.orderNo(), time.plusDays(1)));
        var saved = orderRpc.findByOrderNo(order.orderNo());
        assertTrue(saved.commentStatus());
        var finish = saved.finishTime();
        assertEquals(
            1,
            tradeComponents.get(OrderLogGateway.class).byOrderId(order.id()).stream()
                .filter(v -> v.operateType() == 34)
                .count());
        for (var line : saved.items()) {
            var comment =
                mallComponents
                    .get(ProductCommentGateway.class)
                    .findByOrderItem(line.orderItemId());
            assertEquals("", comment.content());
            assertEquals(5, comment.score());
            assertFalse(comment.anonymous());
        }
        assertEquals(finish, orderRpc.findByOrderNo(order.orderNo()).finishTime());
    }

    @Test
    void systemOriginalRetryRepairsRemoteCommitAfterLostResponse() {
        var order = completedReviewOrderWithTwoItems();
        var service = tradeComponents.get(OrderCommentService.class);
        var cutoff = tradeOrderGateway.databaseNow().plusDays(1);
        loseNextCommentResponse = true;
        assertThrows(
            IllegalStateException.class, () -> service.createSystem(order.orderNo(), cutoff));
        assertFalse(orderRpc.findByOrderNo(order.orderNo()).commentStatus());
        var first = order.items().get(0).orderItemId();
        var comments = mallComponents.get(ProductCommentGateway.class);
        var remoteId = comments.findByOrderItem(first).id();
        assertTrue(service.createSystem(order.orderNo(), cutoff));
        assertEquals(remoteId, comments.findByOrderItem(first).id());
    }

    @Test
    void cashSuccessKeepsPendingBenefitsEventAndRetryUsesFrozenPlan() throws Exception {
        var order = paidOrder(null);
        var service = tradeComponents.get(AfterSaleService.class);
        var sale = service.create(itemRefund(order.orderNo(), "FROZEN-RETRY", 1, "REFUND_ONLY"));
        service.agree(sale.refundNo());
        successfulRefundChannel();
        failNextRefundBenefits = true;
        var executor = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        assertEquals("SUCCESS", executor.execute(sale.refundNo()).status());
        var pending =
            events.pendingEvents().stream()
                .filter(
                    e ->
                        "AFTER_SALE_COMPLETED".equals(e.eventType())
                            && sale.refundNo().equals(e.businessKey()))
                .findFirst()
                .orElseThrow();
        assertFalse(events.memberRefunded.contains(sale.refundNo()));
        assertEquals("REFUNDED", refundRequestGateway.afterSale(sale.refundNo()).status());
        var frozen = refundRequestGateway.afterSale(sale.refundNo());
        deliveryPointGift = true;
        assertEquals("SUCCESS", executor.execute(sale.refundNo()).status());
        assertTrue(
            events.pendingEvents().stream()
                .noneMatch(e -> pending.eventNo().equals(e.eventNo())));
        assertTrue(events.memberRefunded.contains(sale.refundNo()));
        events.replay(pending.eventNo());
        events.drainOrThrow();
        assertEquals(frozen.amount(), refundRequestGateway.afterSale(sale.refundNo()).amount());
        assertEquals(
            frozen.returnUsePoint(),
            refundRequestGateway.afterSale(sale.refundNo()).returnUsePoint());
        assertEquals(
            1,
            orderPersistenceService
                .findByOrderNo(order.orderNo())
                .items()
                .get(0)
                .refundedQuantity());
        assertEquals(1, count("pay_payment_refund"));
        Mockito.verify(refundClient, Mockito.times(1))
            .refund(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void fixedUnitRefundsFreezeAmountsAndLastQuantityClosesRemainder() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, new BigDecimal("50.00")).id());
        var order =
            placement.createDirect(
                new SettlementDirectCmd(
                    1L,
                    List.of(new SettlementDirectCmd.OrderLine(sku, 6)),
                    "UNIT-LAST",
                    coupon.id()));
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        notify.execute(
            new VerifiedPaymentNotification(
                payment.paymentNo(),
                order.orderNo(),
                "ALIPAY",
                "CH-UNIT",
                payment.amount(),
                "N-UNIT"));
        assertEquals(new BigDecimal("10.00"), payment.amount());
        successfulRefundChannel();
        var service = tradeComponents.get(AfterSaleService.class);
        var executor = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        int[] quantities = {2, 3, 1};
        String[] amounts = {"3.32", "4.98", "1.70"};
        int refunded = 0;
        for (int index = 0; index < quantities.length; index++) {
            var sale =
                service.create(
                    itemRefund(
                        order.orderNo(),
                        "UNIT-" + index,
                        quantities[index],
                        "REFUND_ONLY"));
            assertEquals(new BigDecimal(amounts[index]), sale.amount());
            assertEquals(
                sale,
                service.create(
                    itemRefund(
                        order.orderNo(),
                        "UNIT-" + index,
                        quantities[index],
                        "REFUND_ONLY")));
            service.agree(sale.refundNo());
            assertEquals("SUCCESS", executor.execute(sale.refundNo()).status());
            assertEquals("SUCCESS", executor.execute(sale.refundNo()).status());
            refunded += quantities[index];
            var current = orderPersistenceService.findByOrderNo(order.orderNo());
            assertEquals(refunded, current.items().get(0).refundedQuantity());
            assertEquals(index == 2 ? "REFUNDED" : "PAID", current.status());
            assertEquals(
                new BigDecimal(amounts[index]),
                refundRequestGateway.afterSale(sale.refundNo()).amount());
        }
        assertEquals(3, count("pay_payment_refund"));
        assertEquals(
            new BigDecimal("10.00"),
            tradeComponents
                .get(PaymentRefundGateway.class)
                .total(order.orderNo())
                .refundAmount());
    }

    @Test
    void positiveCashOrderAllowsZeroCashQuantitySharesWithoutFakePaymentRefunds() throws Exception {
        var coupon = couponIssuanceService.issue(1L, campaign(10, new BigDecimal("29.99")).id());
        var order =
            placement.createDirect(
                new SettlementDirectCmd(
                    1L,
                    List.of(new SettlementDirectCmd.OrderLine(sku, 3)),
                    "CENT-SHARES",
                    coupon.id()));
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        notify.execute(
            new VerifiedPaymentNotification(
                payment.paymentNo(),
                order.orderNo(),
                "ALIPAY",
                "CH-CENT",
                payment.amount(),
                "N-CENT"));
        assertEquals(new BigDecimal("0.01"), payment.amount());
        successfulRefundChannel();
        var service = tradeComponents.get(AfterSaleService.class);
        var executor = tradeComponents.get(PaymentRefundCreateCmdExe.class);
        for (int index = 0; index < 3; index++) {
            var sale =
                service.create(itemRefund(order.orderNo(), "CENT-" + index, 1, "REFUND_ONLY"));
            assertEquals(new BigDecimal(index == 2 ? "0.01" : "0.00"), sale.amount());
            service.agree(sale.refundNo());
            var result = executor.execute(sale.refundNo());
            assertEquals("SUCCESS", result.status());
            if (index < 2) {
                assertNull(result.refundNo());
                assertEquals(0, count("pay_payment_refund"));
            }
            assertEquals(
                index == 2 ? "REFUNDED" : "PAID",
                orderPersistenceService.findByOrderNo(order.orderNo()).status());
        }
        assertEquals(1, count("pay_payment_refund"));
        Mockito.verify(refundClient, Mockito.times(1))
            .refund(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void manualAdjustmentUsesFrozenGiftRateAndPreservesPromotionAmounts() throws Exception {
        deliveryPointGift = true;
        var order =
            placement.createDirect(
                new SettlementDirectCmd(
                    1L,
                    List.of(new SettlementDirectCmd.OrderLine(sku, 2)),
                    "ADJUST-FROZEN-GIFT",
                    null));
        assertEquals(40L, order.givePoint());
        deliveryPointGift = false;
        var adjusted =
            tradeComponents
                .get(OrderAdjustmentService.class)
                .adjustPrice(
                    new OrderAdjustPriceCmd(
                        order.orderNo(), new BigDecimal("10.00"), 55L));
        assertEquals(20L, adjusted.givePoint());
        assertEquals(new BigDecimal("-10.00"), adjusted.adjustmentAmount());
        assertEquals(new BigDecimal("0.00"), adjusted.discountAmount());
        assertEquals(2, adjusted.pricingSnapshotVersion());
        assertEquals(20L, adjusted.items().get(0).givePoint());
        var gift =
            adjusted.promotions().stream()
                .filter(p -> "ORDER_GIFT".equals(p.promotionType()))
                .findFirst()
                .orElseThrow();
        assertEquals(20L, gift.givePoint());
        assertEquals(20L, gift.items().get(0).givePoint());
        var payment = paymentCreate.execute(order.orderNo(), "ALIPAY");
        notify.execute(
            new VerifiedPaymentNotification(
                payment.paymentNo(),
                order.orderNo(),
                "ALIPAY",
                "CH-ADJUST",
                payment.amount(),
                "N-ADJUST"));
        var sale =
            tradeComponents
                .get(AfterSaleService.class)
                .create(itemRefund(order.orderNo(), "REFUND-ADJUST", 1, "REFUND_ONLY"));
        assertEquals(new BigDecimal("5.00"), sale.amount());
        assertEquals(10L, sale.deductGivePoint());
    }

    @Test
    void historicalUnknownRefundQuantityRequiresReconciliationBeforeNewRequest() throws Exception {
        var order = paidOrder(null);
        var service = tradeComponents.get(AfterSaleService.class);
        var item = orderPersistenceService.findByOrderNo(order.orderNo()).items().get(0);
        sql(
            tradeSource,
            "UPDATE order_item SET refunded_quantity=NULL WHERE id=" + item.orderItemId());
        var quota = service.items(order.orderNo()).get(0);
        assertNull(quota.refundedQuantity());
        assertEquals(0, quota.refundableQuantity());
        assertThrows(
            IllegalStateException.class,
            () ->
                service.create(
                    new AfterSaleCreateCmd(
                        order.orderNo(),
                        "UNKNOWN-QTY",
                        item.orderItemId(),
                        1,
                        "REFUND_ONLY",
                        "原因")));
        sql(
            tradeSource,
            "UPDATE order_item SET refunded_quantity=0 WHERE id=" + item.orderItemId());
        assertEquals(
            new BigDecimal("10.00"),
            service.create(itemRefund(order.orderNo(), "KNOWN-QTY", 1, "REFUND_ONLY"))
                .amount());
    }
}
