package com.lemnt.mall.start.product;

import com.lemnt.mall.application.promotion.cmd.BargainActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.CombinationActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.CombinationProductCmd;
import com.lemnt.mall.application.promotion.cmd.PointActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.PointProductCmd;
import com.lemnt.mall.application.promotion.cmd.PromotionOrderCmd;
import com.lemnt.mall.application.promotion.cmd.SeckillActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.SeckillConfigCreateCmd;
import com.lemnt.mall.application.promotion.cmd.SeckillProductCmd;
import com.lemnt.mall.application.promotion.service.BargainActivityManagementService;
import com.lemnt.mall.application.promotion.service.CombinationActivityManagementService;
import com.lemnt.mall.application.promotion.service.PointActivityManagementService;
import com.lemnt.mall.application.promotion.service.PromotionOrderService;
import com.lemnt.mall.application.promotion.service.SeckillActivityManagementService;
import com.lemnt.mall.application.promotion.service.SeckillConfigManagementService;
import com.lemnt.mall.application.settlement.dto.SettlementPreparationResult;
import com.lemnt.mall.application.settlement.service.SettlementDeliveryService;
import com.lemnt.mall.application.settlement.service.SettlementPreparationService;
import com.lemnt.mall.client.bo.RefundStockBo;
import com.lemnt.mall.domain.member.gateway.MemberLookupGateway;
import com.lemnt.mall.domain.member.gateway.MemberTradeGateway;
import com.lemnt.mall.domain.member.model.MemberPointConfig;
import com.lemnt.mall.domain.member.model.MemberSnapshot;
import com.lemnt.mall.domain.product.model.ProductSpu;
import com.lemnt.mall.domain.promotion.gateway.BargainRecordGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionOrderGateway;
import com.lemnt.mall.domain.promotion.model.BargainRecord;
import com.lemnt.mall.domain.promotion.model.PromotionOrder;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/** 活动订单映射、活动库存预占/恢复、拼团建团与砍价绑定的生产表事务验证。 */
class PromotionOrderPersistenceTest {
    CatalogPersistenceTest f;
    PromotionOrderService orders;
    PromotionOrderGateway mappings;
    SeckillConfigManagementService seckillConfigs;
    SeckillActivityManagementService seckillActivities;
    CombinationActivityManagementService combinationActivities;
    BargainActivityManagementService bargainActivities;
    PointActivityManagementService pointActivities;
    BargainRecordGateway bargainRecords;
    ProductSpu spu;
    Long seckillConfigId;
    Long seckillActivityId;
    Long combinationActivityId;
    Long bargainActivityId;
    Long pointActivityId;
    Long skuId;
    java.util.concurrent.atomic.AtomicLong memberPoints = new java.util.concurrent.atomic.AtomicLong(10000);

    Map<String, SettlementOrder> trades = new HashMap<>();
    AtomicLong tradeIds = new AtomicLong(100);

    @BeforeEach
    void setup() throws Exception {
        f = new CatalogPersistenceTest();
        f.setup();
        for (var sql : List.of("V18__promotion_seckill.sql", "V19__promotion_combination.sql",
                "V20__promotion_bargain.sql", "V21__promotion_point.sql", "V22__promotion_order.sql", "V23__promotion_order_refund.sql")) {
            ScriptUtils.executeSqlScript(f.schemaConnection, new ClassPathResource("sql/" + sql));
        }
        f.components.register(MemberLookupGateway.class,
                memberId -> new MemberSnapshot(memberId, 1, 100, memberPoints.get(), "会员" + memberId, null));
        f.components.register(MemberTradeGateway.class, new MemberTradeGateway() {
            public MemberPointConfig config() { return new MemberPointConfig(false, 1, 0, 0); }
            public void apply(com.lemnt.mall.domain.member.model.MemberTradeChange change) { }
        });
        f.components.register(SettlementDeliveryService.class, Mockito.mock(SettlementDeliveryService.class));
        var settlements = Mockito.mock(SettlementPreparationService.class);
        Map<String, SettlementOrder> snapshots = new HashMap<>();
        AtomicLong orderIds = new AtomicLong(1);
        Mockito.when(settlements.preparePromotion(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.anyList()))
                .thenAnswer(invocation -> {
                    String requestNo = invocation.getArgument(1);
                    var previous = snapshots.get(requestNo);
                    if (previous != null) {
                        return new SettlementPreparationResult(previous, false);
                    }
                    var supplier = (java.util.function.Supplier<com.lemnt.mall.domain.settlement.model.SettlementQuote>)
                            invocation.getArgument(3);
                    var quote = supplier.get();
                    var order = new SettlementOrder(null, "TR" + orderIds.getAndIncrement(),
                            invocation.getArgument(0), quote.payableAmount(), "RESERVING", quote.items(),
                            null, quote.usePoint(), quote.givePoint(), quote.delivery());
                    snapshots.put(requestNo, order);
                    return new SettlementPreparationResult(order, true);
                });
        f.components.register(SettlementPreparationService.class, settlements);
        f.components.register(OrderGateway.class, new OrderGateway() {
            public SettlementOrder save(SettlementOrder order) {
                var saved = new SettlementOrder(tradeIds.getAndIncrement(), order.orderNo(), order.memberId(),
                        order.totalAmount(), "PENDING_PAYMENT", order.items(), order.createTime(),
                        order.usePoint(), order.givePoint(), order.delivery());
                trades.put(order.orderNo(), saved);
                return saved;
            }
            public Long createComment(com.lemnt.mall.domain.product.model.CommentOrderRequest value) { return null; }
            public SettlementOrder findByOrderNo(String orderNo) { return trades.get(orderNo); }
        });
        orders = f.components.get(PromotionOrderService.class);
        mappings = f.components.get(PromotionOrderGateway.class);
        seckillConfigs = f.components.get(SeckillConfigManagementService.class);
        seckillActivities = f.components.get(SeckillActivityManagementService.class);
        combinationActivities = f.components.get(CombinationActivityManagementService.class);
        bargainActivities = f.components.get(BargainActivityManagementService.class);
        pointActivities = f.components.get(PointActivityManagementService.class);
        bargainRecords = f.components.get(BargainRecordGateway.class);
        spu = f.create("PROMOTION_ORDER", f.draft("RED", 10));
        f.productSpuGateway.shelf(spu.id(), true);
        skuId = spu.skus().get(0).id();
        seckillConfigId = seckillConfigs.create(new SeckillConfigCreateCmd("全天", "00:00:00", "23:59:59",
                List.of(), 0));
        var now = LocalDateTime.now();
        seckillActivityId = seckillActivities.create(new SeckillActivityCreateCmd(spu.id(), "秒杀", null,
                now.minusDays(1), now.plusDays(1), 0, List.of(seckillConfigId), 10, 5,
                List.of(new SeckillProductCmd(skuId, new BigDecimal("5.00"), 5))));
        combinationActivityId = combinationActivities.create(new CombinationActivityCreateCmd("拼团", spu.id(),
                10, 5, now.minusDays(1), now.plusDays(1), 2, false, 1,
                List.of(new CombinationProductCmd(skuId, new BigDecimal("4.00")))));
        bargainActivityId = bargainActivities.create(new BargainActivityCreateCmd("砍价", now.minusDays(1),
                now.plusDays(1), spu.id(), skuId, new BigDecimal("10.00"), new BigDecimal("8.00"), 5, 1, 1,
                1, new BigDecimal("2.00"), new BigDecimal("2.00")));
        pointActivityId = pointActivities.create(new PointActivityCreateCmd(spu.id(), null, 1,
                List.of(new PointProductCmd(skuId, 5, 100, new BigDecimal("5.00"), 5))));
    }

    @AfterEach
    void cleanup() throws Exception {
        f.closeSchemaConnection();
    }

    PromotionOrderCmd cmd(String type, Long activityId, Long recordId, int quantity, String requestNo) {
        return new PromotionOrderCmd(1L, type, activityId, recordId, null, skuId, quantity, null, requestNo);
    }

    @Test
    void seckillOrderReservesAndClosedRestoresStockOnce() {
        var order = orders.place(cmd("SECKILL", seckillActivityId, null, 2, "REQ_S1"));
        var mapping = mappings.findByOrderNo(order.orderNo());
        assertEquals("CREATED", mapping.status());
        assertEquals(new BigDecimal("10.00"), order.totalAmount());
        orders.reserved(order.orderNo(), order);
        orders.reserved(order.orderNo(), order);
        assertEquals("RESERVED", mappings.findByOrderNo(order.orderNo()).status());
        assertEquals(3, seckillActivities.detail(seckillActivityId).stock());
        orders.closed(order.orderNo());
        orders.closed(order.orderNo());
        assertEquals("CLOSED", mappings.findByOrderNo(order.orderNo()).status());
        assertEquals(5, seckillActivities.detail(seckillActivityId).stock());
    }

    @Test
    void sameRequestReturnsSameOrderAndDifferentContentIsRejected() {
        var first = orders.place(cmd("SECKILL", seckillActivityId, null, 1, "REQ_IDEM"));
        var second = orders.place(cmd("SECKILL", seckillActivityId, null, 1, "REQ_IDEM"));
        assertEquals(first.orderNo(), second.orderNo());
        assertNotNull(mappings.findByRequest(1L, "REQ_IDEM").id());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> orders.place(cmd("SECKILL", seckillActivityId, null, 2, "REQ_IDEM")));
    }

    @Test
    void combinationOrderCreatesRecordOnPaidOnce() {
        var order = orders.place(cmd("COMBINATION", combinationActivityId, null, 1, "REQ_C1"));
        orders.reserved(order.orderNo(), order);
        orders.paid(order.orderNo(), order);
        orders.paid(order.orderNo(), order);
        var mapping = mappings.findByOrderNo(order.orderNo());
        assertEquals("PAID", mapping.status());
        assertNotNull(mapping.activityRecordId());
        assertEquals(1, f.components.get(com.lemnt.mall.application.promotion.service.CombinationRecordService.class)
                .byHead(mapping.activityRecordId()).size());
    }

    @Test
    void bargainOrderBindsRecordOnReservedAndClosedRestoresStock() {
        var record = bargainRecords.save(new BargainRecord(null, 1L, bargainActivityId, spu.id(), skuId,
                new BigDecimal("10.00"), new BigDecimal("8.00"), 2, LocalDateTime.now(), null));
        var order = orders.place(cmd("BARGAIN", null, record.id(), 1, "REQ_B1"));
        assertEquals(new BigDecimal("8.00"), order.totalAmount());
        orders.reserved(order.orderNo(), order);
        assertEquals("RESERVED", mappings.findByOrderNo(order.orderNo()).status());
        assertNotNull(bargainRecords.find(record.id()).orderId());
        assertEquals(4, bargainActivities.detail(bargainActivityId).stock());
        orders.closed(order.orderNo());
        assertEquals(5, bargainActivities.detail(bargainActivityId).stock());
    }

    @Test
    void pointOrderUsesPointsAndRefundRestoresStockByDelta() {
        var order = orders.place(cmd("POINT", pointActivityId, null, 2, "REQ_P1"));
        assertEquals(200L, order.usePoint());
        assertEquals(new BigDecimal("10.00"), order.totalAmount());
        orders.reserved(order.orderNo(), order);
        orders.paid(order.orderNo(), order);
        assertEquals(3, pointActivities.detail(pointActivityId).stock());
        orders.refunded(order.orderNo(), "RF1", order,
                List.of(new RefundStockBo(skuId, 1)));
        orders.refunded(order.orderNo(), "RF1", order,
                List.of(new RefundStockBo(skuId, 1)));
        assertEquals(4, pointActivities.detail(pointActivityId).stock());
        orders.refunded(order.orderNo(), "RF2", order,
                List.of(new RefundStockBo(skuId, 1)));
        assertEquals(5, pointActivities.detail(pointActivityId).stock());
        var refunded = new SettlementOrder(order.id(), order.orderNo(), order.memberId(), order.totalAmount(),
                "REFUNDED", order.items(), order.createTime(), order.usePoint(), order.givePoint(),
                order.delivery());
        orders.refunded(order.orderNo(), "RF3", refunded, List.of(new RefundStockBo(skuId, 2)));
        assertEquals(5, pointActivities.detail(pointActivityId).stock());
        assertEquals("REFUNDED", mappings.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void zeroCashPointOrderUsesPointsAndInsufficientPointsIsRejected() {
        pointActivities.close(pointActivityId);
        var zero = pointActivities.create(new PointActivityCreateCmd(spu.id(), null, 1,
                List.of(new PointProductCmd(skuId, 5, 100, BigDecimal.ZERO, 5))));
        var order = orders.place(cmd("POINT", zero, null, 1, "REQ_ZERO"));
        assertEquals(0, order.totalAmount().signum());
        assertEquals(100L, order.usePoint());
        orders.reserved(order.orderNo(), order);
        orders.paid(order.orderNo(), order);
        assertEquals("PAID", mappings.findByOrderNo(order.orderNo()).status());
        assertEquals(4, pointActivities.detail(zero).stock());
        var refunded = new SettlementOrder(order.id(), order.orderNo(), order.memberId(), order.totalAmount(),
                "REFUNDED", order.items(), order.createTime(), order.usePoint(), order.givePoint(),
                order.delivery());
        orders.refunded(order.orderNo(), "RF-ZERO", refunded, List.of(new RefundStockBo(skuId, 1)));
        assertEquals(5, pointActivities.detail(zero).stock());
        assertEquals("REFUNDED", mappings.findByOrderNo(order.orderNo()).status());
        var alt = f.create("POINT_ALT", f.draft("BLUE", 10));
        f.productSpuGateway.shelf(alt.id(), true);
        var lowActivity = pointActivities.create(new PointActivityCreateCmd(alt.id(), null, 1,
                List.of(new PointProductCmd(alt.skus().get(0).id(), 5, 100, new BigDecimal("5.00"), 5))));
        memberPoints.set(50L);
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> orders.place(new PromotionOrderCmd(1L, "POINT", lowActivity, null, null,
                        alt.skus().get(0).id(), 2, null, "REQ_LOW")));
    }

    @Test
    void zeroCashPartialRefundReturnsPointsByQuantity() {
        pointActivities.close(pointActivityId);
        var zero = pointActivities.create(new PointActivityCreateCmd(spu.id(), null, 1,
                List.of(new PointProductCmd(skuId, 5, 100, BigDecimal.ZERO, 5))));
        var order = orders.place(cmd("POINT", zero, null, 2, "REQ_ZPART"));
        assertEquals(200L, order.usePoint());
        orders.reserved(order.orderNo(), order);
        orders.paid(order.orderNo(), order);
        assertEquals(3, pointActivities.detail(zero).stock());
        var first = orders.refunded(order.orderNo(), "RF-P1", order,
                List.of(new RefundStockBo(skuId, 1)));
        assertEquals(100L, first.refundPoints());
        assertEquals(4, pointActivities.detail(zero).stock());
        assertEquals("PAID", mappings.findByOrderNo(order.orderNo()).status());
        var refunded = new SettlementOrder(order.id(), order.orderNo(), order.memberId(), order.totalAmount(),
                "REFUNDED", order.items(), order.createTime(), order.usePoint(), order.givePoint(),
                order.delivery());
        var second = orders.refunded(order.orderNo(), "RF-P2", refunded,
                List.of(new RefundStockBo(skuId, 1)));
        assertEquals(100L, second.refundPoints());
        assertEquals(5, pointActivities.detail(zero).stock());
        assertEquals("REFUNDED", mappings.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void mappingRequestNumberIsUnique() {
        var mapping = new PromotionOrder(null, "TR-X", "REQ-U", 1L, "SECKILL", seckillActivityId, null,
                null, skuId, 1, BigDecimal.ONE, 0L, 0, "CREATED", null, null, null);
        mappings.save(mapping);
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> mappings.save(new PromotionOrder(null, "TR-Y", "REQ-U", 1L, "SECKILL",
                        seckillActivityId, null, null, skuId, 1, BigDecimal.ONE, 0L, 0, "CREATED",
                        null, null, null)));
    }
}
