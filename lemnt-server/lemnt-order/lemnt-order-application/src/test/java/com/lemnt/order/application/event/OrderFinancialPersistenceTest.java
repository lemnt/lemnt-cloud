package com.lemnt.order.application.event;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.application.aftersale.cmd.AfterSaleCreateCmd;
import com.lemnt.order.application.aftersale.service.AfterSaleRefundCoordinator;
import com.lemnt.order.application.aftersale.service.AfterSaleService;
import com.lemnt.order.application.brokerage.cmd.BrokerageWithdrawAuditCmd;
import com.lemnt.order.application.brokerage.service.BrokerageService;
import com.lemnt.order.application.brokerage.service.BrokerageWithdrawService;
import com.lemnt.order.application.dubbo.TradeOrderClientImpl;
import com.lemnt.order.application.event.service.PayRefundSuccessEventHandler;
import com.lemnt.order.application.event.service.PaySuccessEventHandler;
import com.lemnt.order.application.fulfillment.cmd.ShipmentCreateCmd;
import com.lemnt.order.application.fulfillment.service.OrderFulfillmentService;
import com.lemnt.order.application.order.cmd.OrderAdjustPriceCmd;
import com.lemnt.order.application.order.service.OrderAdjustmentService;
import com.lemnt.order.application.order.service.OrderBenefitsService;
import com.lemnt.order.application.order.service.OrderCommentService;
import com.lemnt.order.application.order.service.OrderLifecycleService;
import com.lemnt.order.application.order.service.OrderPaidCompletionService;
import com.lemnt.order.application.order.service.OrderPaymentPreparationService;
import com.lemnt.order.application.order.service.OrderPersistenceService;
import com.lemnt.order.application.order.service.OrderRemoteService;
import com.lemnt.order.application.order.service.OrderSubmissionService;
import com.lemnt.order.application.settlement.cmd.SettlementDirectCmd;
import com.lemnt.order.application.settlement.service.LocalDeliveryPriceService;
import com.lemnt.order.application.settlement.service.OrderPlacementService;
import com.lemnt.order.application.support.MemorySettlementGateway;
import com.lemnt.order.application.support.SqlTestSupport;
import com.lemnt.order.application.support.TableTestComponents;
import com.lemnt.order.client.TradeOrderClient;
import com.lemnt.order.client.event.AfterSaleCompletedEvent;
import com.lemnt.order.client.event.OrderPaidEvent;
import com.lemnt.order.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.order.domain.aftersale.model.AfterSale;
import com.lemnt.order.domain.brokerage.gateway.BrokerageWalletGateway;
import com.lemnt.order.domain.brokerage.gateway.BrokerageWithdrawGateway;
import com.lemnt.order.domain.brokerage.model.BrokerageWithdraw;
import com.lemnt.order.domain.cart.gateway.CartSnapshotGateway;
import com.lemnt.order.domain.config.gateway.TradeConfigGateway;
import com.lemnt.order.domain.delivery.gateway.DeliveryExpressGateway;
import com.lemnt.order.domain.delivery.gateway.PickUpStoreGateway;
import com.lemnt.order.domain.delivery.model.DeliveryExpress;
import com.lemnt.order.domain.event.gateway.MessageInboxGateway;
import com.lemnt.order.domain.event.gateway.MessageOutboxGateway;
import com.lemnt.order.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.order.domain.fulfillment.gateway.ShipmentGateway;
import com.lemnt.order.domain.inventory.gateway.InventoryGateway;
import com.lemnt.order.domain.member.gateway.MemberDeliveryAddressGateway;
import com.lemnt.order.domain.member.gateway.MemberLockGateway;
import com.lemnt.order.domain.member.gateway.MemberLookupGateway;
import com.lemnt.order.domain.member.gateway.MemberTradeGateway;
import com.lemnt.order.domain.member.model.MemberSnapshot;
import com.lemnt.order.domain.order.gateway.OrderBenefitsGateway;
import com.lemnt.order.domain.order.gateway.OrderBenefitsProgressGateway;
import com.lemnt.order.domain.order.gateway.OrderRecoveryGateway;
import com.lemnt.order.domain.order.gateway.PromotionResourceGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.model.TradeOperator;
import com.lemnt.order.domain.order.model.TradeOrder;
import com.lemnt.order.domain.order.model.TradeOrderItem;
import com.lemnt.order.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.order.domain.payment.model.PaymentRecord;
import com.lemnt.order.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.order.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.order.domain.product.gateway.ProductCatalogGateway;
import com.lemnt.order.domain.promotion.gateway.PromotionPricingGateway;
import com.lemnt.order.domain.settlement.gateway.SettlementGateway;
import com.lemnt.order.domain.settlement.model.SettlementItem;
import com.lemnt.order.domain.settlement.model.SettlementQuote;
import com.lemnt.order.infrastructure.product.gateway.ProductCatalogGatewayImpl;
import com.lemnt.order.infrastructure.promotion.converter.PromotionQuoteConverter;
import com.lemnt.order.infrastructure.promotion.gateway.PromotionPricingGatewayImpl;
import com.lemnt.pay.client.event.PayRefundSuccessEvent;
import com.lemnt.pay.client.event.PaySuccessEvent;
import com.lemnt.product.client.ProductCatalogClient;
import com.lemnt.product.client.dto.ProductSnapshotDTO;
import com.lemnt.promotion.client.PromotionCalcClient;
import com.lemnt.promotion.client.bo.PromotionCalculateBo;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 只有订单库的真实DDL/Mapper/事务；资金查询为RPC传输替身，不能依靠支付表JOIN通过。
 */
class OrderFinancialPersistenceTest {
    JdbcDataSource source;
    Connection ddl;
    JdbcTemplate jdbc;
    TransactionTemplate tx;
    TableTestComponents components;
    final TradePaymentGateway payments = mock(TradePaymentGateway.class);
    final PaymentRefundGateway refunds = mock(PaymentRefundGateway.class);
    OrderPersistenceService orders;
    AfterSaleService afterSales;
    AfterSaleRefundCoordinator coordinator;

    @BeforeEach
    void setup() throws Exception {
        source = new JdbcDataSource();
        source.setURL(
            "jdbc:h2:mem:order_"
                + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
        ddl = source.getConnection();
        ScriptUtils.executeSqlScript(ddl, new ClassPathResource("order-sql/lemnt_order.sql"));
        jdbc = new JdbcTemplate(source);
        tx = new TransactionTemplate(new DataSourceTransactionManager(source));
        components = new TableTestComponents(SqlTestSupport.session("order", source), source);
        components.register(TradePaymentGateway.class, payments);
        components.register(PaymentRefundGateway.class, refunds);
        components.register(BrokerageService.class, mock(BrokerageService.class));
        orders = components.get(OrderPersistenceService.class);
        afterSales = components.get(AfterSaleService.class);
        coordinator = components.get(AfterSaleRefundCoordinator.class);
    }

    @AfterEach
    void close() throws Exception {
        ddl.close();
    }

    TradeOrder seed(String no, String cash, int quantity, String state) {
        var amount = new BigDecimal(cash);
        var z = BigDecimal.ZERO.setScale(2);
        var item =
            new TradeOrderItem(
                2L, "SKU", "原商品", amount, quantity, amount, amount, z, 3L, "原SPU",
                List.of(), null, null, false, z, z, z, z, 0L, 0L, 0);
        orders.save(
            new TradeOrder(
                null,
                no,
                7L,
                amount,
                state,
                List.of(item),
                null,
                0,
                0,
                null,
                false,
                null,
                Map.of(),
                amount,
                z,
                z,
                z,
                z,
                "COMPLETE",
                1,
                List.of()));
        var saved = orders.findByOrderNo(no);
        when(payments.findByOrderNo(no))
            .thenReturn(
                List.of(
                    new PaymentRecord(
                        null, "P" + no, no, "ALIPAY", "APP", "TX" + no, amount,
                        "SUCCESS", null)));
        tx.executeWithoutResult(
            s ->
                components
                    .get(InventoryConfirmationGateway.class)
                    .confirmInventory(no, LocalDateTime.now()));
        return saved;
    }

    AfterSale request(String no, String key, int quantity) {
        var line = orders.findByOrderNo(no).items().get(0);
        var af =
            afterSales.create(
                new AfterSaleCreateCmd(
                    no, key, line.orderItemId(), quantity, "REFUND_ONLY", "部分退款"));
        afterSales.agree(af.refundNo());
        return components.get(AfterSaleGateway.class).afterSale(af.refundNo());
    }

    PaymentRefund cash(AfterSale af) {
        return new PaymentRefund(
            "PR" + af.refundNo(),
            af.refundNo(),
            af.paymentNo(),
            af.orderNo(),
            "ALIPAY",
            af.amount(),
            "SUCCESS",
            "CHANNEL" + af.refundNo());
    }

    void complete(AfterSale af) {
        coordinator.prepare(af.refundNo(), TradeOperator.system());
        var fact = cash(af);
        when(refunds.findByAfterSaleNo(af.refundNo())).thenReturn(fact);
        coordinator.cashSucceeded(fact);
    }

    CommerceEventEnvelope envelope(String type, String key, String no, Object body) {
        return new CommerceEventEnvelope(
            "E" + UUID.randomUUID(),
            type,
            1,
            key,
            no,
            Instant.now(),
            null,
            JsonUtils.toJsonStr(body));
    }

    int count(String table) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }

    @Test
    void onlyOneProcessingAfterSaleAndAmountsAreFrozenAtCreation() {
        seed("O1", "10.00", 6, "PAID");
        var first = request("O1", "R1", 2);
        assertEquals(new BigDecimal("3.32"), first.amount());
        assertThrows(IllegalStateException.class, () -> request("O1", "R2", 1));
        coordinator.prepare(first.refundNo(), TradeOperator.system());
        assertEquals(new BigDecimal("3.32"), coordinator.plan(first.refundNo()).amount());
        assertEquals(0, orders.findByOrderNo("O1").items().get(0).refundedQuantity());
        assertEquals(0, count("order_message_outbox"));
    }

    @Test
    void partialRefundsUseFixedUnitAmountAndLastQuantityClosesRemainder() {
        seed("O1", "10.00", 6, "PAID");
        int[] quantities = {2, 3, 1};
        String[] cash = {"3.32", "4.98", "1.70"};
        for (int n = 0; n < 3; n++) {
            var af = request("O1", "R" + n, quantities[n]);
            assertEquals(new BigDecimal(cash[n]), af.amount());
            complete(af);
            coordinator.cashSucceeded(cash(af));
        }
        assertEquals("REFUNDED", orders.findByOrderNo("O1").status());
        assertEquals(6, orders.findByOrderNo("O1").items().get(0).refundedQuantity());
        assertEquals(3, count("order_message_outbox"));
        assertEquals(
            new BigDecimal("10.00"),
            jdbc.queryForObject("SELECT SUM(amount) FROM order_after_sale", BigDecimal.class));
    }

    @Test
    void zeroCashSharesCreateNoCashRequestAndCashAloneCannotFinishQuantity() {
        seed("O1", "0.01", 3, "PAID");
        for (int n = 0; n < 2; n++) {
            var af = request("O1", "R" + n, 1);
            assertEquals(0, af.amount().signum());
            coordinator.prepare(af.refundNo(), TradeOperator.system());
            assertEquals("PAID", orders.findByOrderNo("O1").status());
        }
        var last = request("O1", "R2", 1);
        assertEquals(new BigDecimal("0.01"), last.amount());
        complete(last);
        assertEquals("REFUNDED", orders.findByOrderNo("O1").status());
        assertEquals(3, count("order_message_outbox"));
    }

    @Test
    void eventWriteFailureRollsBackAfterSaleQuantityAndCashFactReceipt() {
        seed("O1", "10.00", 2, "PAID");
        var af = request("O1", "R", 1);
        coordinator.prepare(af.refundNo(), TradeOperator.system());
        var fact = cash(af);
        when(refunds.findByAfterSaleNo(af.refundNo())).thenReturn(fact);
        jdbc.execute(
            "ALTER TABLE order_message_outbox ADD CONSTRAINT reject_af CHECK (event_type <> 'AFTER_SALE_COMPLETED')");
        var event =
            envelope(
                CommerceEventTopics.PAY_REFUND_SUCCESS,
                fact.refundNo(),
                "O1",
                new PayRefundSuccessEvent(
                    fact.refundNo(),
                    af.refundNo(),
                    fact.paymentNo(),
                    "O1",
                    7L,
                    "ALIPAY",
                    af.amount(),
                    fact.channelRefundNo()));
        assertThrows(
            RuntimeException.class,
            () -> components.get(PayRefundSuccessEventHandler.class).execute(event));
        assertEquals("REFUNDING", coordinator.plan(af.refundNo()).status());
        assertEquals(0, orders.findByOrderNo("O1").items().get(0).refundedQuantity());
        assertEquals(0, count("order_message_inbox"));
        assertEquals(0, count("order_message_outbox"));
        jdbc.execute("ALTER TABLE order_message_outbox DROP CONSTRAINT reject_af");
        components.get(PayRefundSuccessEventHandler.class).execute(event);
        components.get(PayRefundSuccessEventHandler.class).execute(event);
        assertEquals(1, orders.findByOrderNo("O1").items().get(0).refundedQuantity());
        assertEquals(1, count("order_message_outbox"));
    }

    @Test
    void originalAfterSaleEventIsReplayedWithoutChangingEarlierFullRefundFlag() {
        seed("O1", "10.00", 2, "PAID");
        var first = request("O1", "R1", 1);
        complete(first);
        String payload =
            jdbc.queryForObject(
                "SELECT payload FROM order_message_outbox WHERE business_key=?",
                String.class,
                first.refundNo());
        var event = JsonUtils.toBean(payload, CommerceEventEnvelope.class);
        assertFalse(
            JsonUtils.toBean(event.payload(), AfterSaleCompletedEvent.class).fullyRefunded());
        complete(request("O1", "R2", 1));
        assertEquals("REFUNDED", orders.findByOrderNo("O1").status());
        assertTrue(
            components
                .get(MessageOutboxGateway.class)
                .replay(CommerceEventTopics.AFTER_SALE_COMPLETED, first.refundNo()));
        assertEquals(
            payload,
            jdbc.queryForObject(
                "SELECT payload FROM order_message_outbox WHERE business_key=?",
                String.class,
                first.refundNo()));
    }

    @Test
    void positiveOrderCannotUseZeroPaymentAndZeroOrderProducesOnlyBusinessEvent() {
        seed("POS", "10.00", 1, "PENDING_PAYMENT");
        assertThrows(
            IllegalStateException.class,
            () -> components.get(OrderLifecycleService.class).zeroPay("POS"));
        assertEquals("PENDING_PAYMENT", orders.findByOrderNo("POS").status());
        seed("ZERO", "0.00", 1, "PENDING_PAYMENT");
        components.get(OrderLifecycleService.class).zeroPay("ZERO");
        components.get(OrderLifecycleService.class).zeroPay("ZERO");
        assertEquals("PAID", orders.findByOrderNo("ZERO").status());
        assertEquals(1, count("order_message_outbox"));
        var event =
            JsonUtils.toBean(
                jdbc.queryForObject(
                    "SELECT payload FROM order_message_outbox", String.class),
                CommerceEventEnvelope.class);
        var paid = JsonUtils.toBean(event.payload(), OrderPaidEvent.class);
        assertEquals(1, paid.items().size());
        assertEquals(2L, paid.items().get(0).skuId());
    }

    @Test
    void paidFactIsVerifiedAndBusinessEventSharesOrderTransaction() {
        seed("O1", "10.00", 2, "PENDING_PAYMENT");
        var event =
            envelope(
                CommerceEventTopics.PAY_SUCCESS,
                "PO1",
                "O1",
                new PaySuccessEvent(
                    "PO1", "O1", 7L, "ALIPAY", new BigDecimal("10.00"), "TXO1"));
        jdbc.execute(
            "ALTER TABLE order_message_outbox ADD CONSTRAINT reject_paid CHECK (event_type <> 'ORDER_PAID')");
        assertThrows(
            RuntimeException.class,
            () -> components.get(PaySuccessEventHandler.class).execute(event));
        assertEquals("PENDING_PAYMENT", orders.findByOrderNo("O1").status());
        assertEquals(0, count("order_message_inbox"));
        jdbc.execute("ALTER TABLE order_message_outbox DROP CONSTRAINT reject_paid");
        components.get(PaySuccessEventHandler.class).execute(event);
        components.get(PaySuccessEventHandler.class).execute(event);
        assertEquals("PAID", orders.findByOrderNo("O1").status());
        assertEquals(1, count("order_message_outbox"));
        assertEquals(1, count("order_message_inbox"));
    }

    @Test
    void paymentPreparationBlocksPriceChangeEvenWhenRemoteCreateResponseIsUnknown() {
        seed("O1", "10.00", 1, "PENDING_PAYMENT");
        when(payments.findByOrderNo("O1")).thenReturn(List.of());
        components.get(OrderPaymentPreparationService.class).prepare("O1");
        var adjustment = components.get(OrderAdjustmentService.class);
        ReflectionTestUtils.setField(
            adjustment, "inbox", components.get(MessageInboxGateway.class));
        assertThrows(
            RuntimeException.class,
            () ->
                adjustment.adjustPrice(
                    new OrderAdjustPriceCmd("O1", new BigDecimal("8.00"), 9L)));
        assertEquals(new BigDecimal("10.00"), orders.findByOrderNo("O1").totalAmount());
    }

    @Test
    void concurrentAfterSaleRequestsKeepOneActiveAndKnownHistoryIsRequired() throws Exception {
        seed("O1", "10.00", 6, "PAID");
        var latch = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            var jobs =
                IntStream.range(0, 2)
                    .mapToObj(
                        i ->
                            pool.submit(
                                () -> {
                                    latch.await();
                                    try {
                                        return request("O1", "R" + i, 4)
                                            != null;
                                    } catch (IllegalStateException expected) {
                                        return false;
                                    }
                                }))
                    .toList();
            latch.countDown();
            int ok = 0;
            for (var j : jobs) if (j.get(10, TimeUnit.SECONDS)) ok++;
            assertEquals(1, ok);
            assertEquals(1, count("order_after_sale"));
        } finally {
            pool.shutdownNow();
        }
        jdbc.update("UPDATE order_item SET refunded_quantity=NULL");
        assertThrows(IllegalStateException.class, () -> request("O1", "UNKNOWN", 1));
    }

    @Test
    void incompleteHistoricalPricingStopsNewAfterSaleBeforePersisting() {
        seed("O1", "10.00", 2, "PAID");
        jdbc.update("UPDATE order_info SET pricing_snapshot_status='LEGACY_PARTIAL'");
        assertThrows(IllegalStateException.class, () -> request("O1", "HISTORY", 1));
        assertEquals(0, count("order_after_sale"));
    }

    @Test
    void recoveryQueriesUseOnlyOwnedTablesAndPersistedPendingStates() {
        seed("O1", "10.00", 2, "PAID");
        jdbc.update("DELETE FROM order_inventory_confirmation");
        var recovery = components.get(OrderRecoveryGateway.class);
        assertEquals(List.of("O1"), recovery.pendingPaid(50));
        tx.executeWithoutResult(
            s ->
                components
                    .get(InventoryConfirmationGateway.class)
                    .confirmInventory("O1", LocalDateTime.now()));
        var af = request("O1", "R1", 1);
        coordinator.prepare(af.refundNo(), TradeOperator.system());
        assertEquals(List.of(af.refundNo()), recovery.pendingRefunds(50));
        assertTrue(recovery.pendingWithdrawals(50).isEmpty());
    }

    @Test
    void withdrawalUnknownWalletResultKeepsApprovedStageAndRejectCannotReturnCommission() {
        var gateway = components.get(BrokerageWithdrawGateway.class);
        var withdrawal =
            gateway.save(
                new BrokerageWithdraw(
                    null,
                    7L,
                    new BigDecimal("10.00"),
                    new BigDecimal("0.50"),
                    new BigDecimal("50.00"),
                    1,
                    "本人钱包",
                    "7",
                    null,
                    null,
                    null,
                    0,
                    null,
                    null,
                    null,
                    LocalDateTime.now()));
        var credits = mock(BrokerageWalletGateway.class);
        components.register(BrokerageWalletGateway.class, credits);
        components.register(TradeConfigGateway.class, mock(TradeConfigGateway.class));
        var service = components.get(BrokerageWithdrawService.class);
        var approve = new BrokerageWithdrawAuditCmd(withdrawal.id(), true, "通过", 9L);
        doThrow(new IllegalStateException("钱包已入账但响应丢失")).doNothing().when(credits).credit(any());
        assertThrows(IllegalStateException.class, () -> service.audit(approve));
        assertEquals(10, gateway.find(withdrawal.id()).status());
        assertEquals(
            List.of(withdrawal.id()),
            components.get(OrderRecoveryGateway.class).pendingWithdrawals(50));
        assertThrows(
            ServiceException.class,
            () ->
                service.audit(
                    new BrokerageWithdrawAuditCmd(withdrawal.id(), false, "驳回", 9L)));
        verify(components.get(BrokerageService.class), never())
            .returnRejectedWithdraw(any(), any(), any());
        assertTrue(service.audit(approve));
        assertTrue(service.audit(approve));
        assertEquals(11, gateway.find(withdrawal.id()).status());
        verify(credits, times(2)).credit(any());
    }

    @Test
    void directSettlementUsesTypedClientQuoteAndPersistsStableOriginalSpecSnapshot() {
        var productClient = mock(ProductCatalogClient.class);
        var product =
            new ProductSnapshotDTO(
                2L,
                "SKU",
                "原商品",
                new BigDecimal("10.00"),
                10,
                3L,
                "原SPU",
                List.of(new ProductSnapshotDTO.Spec(4L, "颜色", 5L, "黑色")),
                "https://example.com/old.jpg");
        when(productClient.findOnShelfBatch(any())).thenReturn(List.of(product));
        var catalog = new ProductCatalogGatewayImpl();
        ReflectionTestUtils.setField(catalog, "client", productClient);
        components.register(ProductCatalogGateway.class, catalog);
        var calc = mock(PromotionCalcClient.class);
        when(calc.calculate(any()))
            .thenAnswer(
                call -> {
                    PromotionCalculateBo request = call.getArgument(0);
                    assertEquals(7L, request.memberId());
                    var source = request.items().get(0);
                    assertEquals(4L, source.specs().get(0).specId());
                    var amount = source.lineAmount();
                    var z = BigDecimal.ZERO.setScale(2);
                    var line =
                        new SettlementItem(
                            source.productId(),
                            source.productCode(),
                            source.productName(),
                            source.unitPrice(),
                            source.quantity(),
                            amount,
                            amount,
                            z,
                            source.spuId(),
                            source.spuName(),
                            List.of(new SettlementItem.Spec(4L, "颜色", 5L, "黑色")),
                            source.picUrl(),
                            null,
                            null,
                            z,
                            z,
                            z,
                            z,
                            0L,
                            0L,
                            0);
                    return PromotionQuoteConverter.to(
                        new SettlementQuote(
                            List.of(line),
                            amount,
                            z,
                            amount,
                            null,
                            0,
                            0,
                            null,
                            0,
                            false,
                            Map.of(),
                            List.of(),
                            z,
                            z));
                });
        var prices = new PromotionPricingGatewayImpl();
        ReflectionTestUtils.setField(prices, "client", calc);
        components.register(PromotionPricingGateway.class, prices);
        components.register(MemberLookupGateway.class, id -> new MemberSnapshot(id, 1));
        components.register(MemberLockGateway.class, id -> {
        });
        var members = mock(MemberTradeGateway.class);
        components.register(MemberTradeGateway.class, members);
        components.register(
            MemberDeliveryAddressGateway.class, mock(MemberDeliveryAddressGateway.class));
        components.register(LocalDeliveryPriceService.class, mock(LocalDeliveryPriceService.class));
        var cache = new MemorySettlementGateway(new ConcurrentHashMap<>());
        components.register(SettlementGateway.class, cache);
        components.register(CartSnapshotGateway.class, mock(CartSnapshotGateway.class));
        components.register(PromotionResourceGateway.class, mock(PromotionResourceGateway.class));
        components.register(
            OrderBenefitsProgressGateway.class, mock(OrderBenefitsProgressGateway.class));
        var inventory = mock(InventoryGateway.class);
        when(inventory.reserveStock(any(), eq(2L), eq(2))).thenReturn(true);
        components.register(InventoryGateway.class, inventory);
        components.register(OrderBenefitsGateway.class, components.get(OrderBenefitsService.class));
        var provider =
            new TradeOrderClientImpl(
                components.get(OrderSubmissionService.class),
                orders,
                components.get(OrderRemoteService.class),
                components.get(TradeOrderGateway.class),
                mock(TradeConfigGateway.class),
                mock(PickUpStoreGateway.class));
        components.register(TradeOrderClient.class, provider);
        var placement = components.get(OrderPlacementService.class);
        var cmd =
            new SettlementDirectCmd(
                7L, List.of(new SettlementDirectCmd.OrderLine(2L, 2)), "DIRECT", null);
        var first = placement.createDirect(cmd);
        var repeated = placement.createDirect(cmd);
        assertEquals(first.orderNo(), repeated.orderNo());
        assertEquals("PENDING_PAYMENT", repeated.status());
        assertEquals(new BigDecimal("20.00"), repeated.totalAmount());
        assertEquals(1, count("order_info"));
        assertEquals(1, count("order_item"));
        var saved = orders.findByOrderNo(first.orderNo());
        assertEquals("COMPLETE", saved.pricingSnapshotStatus());
        assertEquals(4L, saved.items().get(0).specs().get(0).specId());
        assertEquals(5L, saved.items().get(0).specs().get(0).specValueId());
        verify(inventory, times(1)).reserveStock(first.orderNo(), 2L, 2);
    }

    @Test
    void commissionFailureRollsBackInventoryMarkerAndOriginalRetryFinishesOnce() {
        seed("O1", "10.00", 1, "PAID");
        jdbc.update("DELETE FROM order_inventory_confirmation");
        var brokerage = components.get(BrokerageService.class);
        doThrow(new IllegalStateException("佣金写入失败"))
            .doReturn(0)
            .when(brokerage)
            .addOrderCommission("O1");
        var service = components.get(OrderPaidCompletionService.class);
        assertThrows(IllegalStateException.class, () -> service.finish("O1", null));
        assertFalse(components.get(InventoryConfirmationGateway.class).inventoryConfirmed("O1"));
        service.finish("O1", null);
        service.finish("O1", null);
        assertTrue(components.get(InventoryConfirmationGateway.class).inventoryConfirmed("O1"));
        verify(brokerage, times(2)).addOrderCommission("O1");
    }

    private OrderFulfillmentService shipping() {
        components
            .get(DeliveryExpressGateway.class)
            .insert(new DeliveryExpress(null, "SF", "顺丰", null, 0, 0));
        return components.get(OrderFulfillmentService.class);
    }

    private ShipmentCreateCmd shipment() {
        return new ShipmentCreateCmd("O1", "SHIP1", "SF", "SF123", "收件人", "13800000000", "原地址");
    }

    @Test
    void shipmentAndReceiptPublishFactsWithTheirOwnedStateAndReplayExactlyOnce() {
        seed("O1", "10.00", 1, "PAID");
        var fulfillment = shipping();
        fulfillment.ship(shipment());
        fulfillment.ship(shipment());
        assertEquals("SHIPPED", orders.findByOrderNo("O1").status());
        assertEquals(1, count("order_message_outbox"));
        var first = fulfillment.receive("O1");
        var retry = fulfillment.receive("O1");
        assertEquals(first.receivedAt(), retry.receivedAt());
        assertEquals("COMPLETED", orders.findByOrderNo("O1").status());
        assertEquals(2, count("order_message_outbox"));
    }

    @Test
    void shippingOrReceiptEventFailureRollsBackWaybillAndStatus() {
        seed("O1", "10.00", 1, "PAID");
        var fulfillment = shipping();
        jdbc.execute(
            "ALTER TABLE order_message_outbox ADD CONSTRAINT reject_shipped CHECK (event_type <> 'ORDER_SHIPPED')");
        assertThrows(RuntimeException.class, () -> fulfillment.ship(shipment()));
        assertEquals("PAID", orders.findByOrderNo("O1").status());
        assertEquals(0, count("order_shipment"));
        jdbc.execute("ALTER TABLE order_message_outbox DROP CONSTRAINT reject_shipped");
        fulfillment.ship(shipment());
        jdbc.execute(
            "ALTER TABLE order_message_outbox ADD CONSTRAINT reject_completed CHECK (event_type <> 'ORDER_COMPLETED')");
        assertThrows(RuntimeException.class, () -> fulfillment.receive("O1"));
        assertEquals("SHIPPED", orders.findByOrderNo("O1").status());
        assertNull(components.get(ShipmentGateway.class).shipment("O1").receivedAt());
        jdbc.execute("ALTER TABLE order_message_outbox DROP CONSTRAINT reject_completed");
        fulfillment.receive("O1");
        assertEquals("COMPLETED", orders.findByOrderNo("O1").status());
    }
}
