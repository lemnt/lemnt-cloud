package com.lemnt.order.start.product;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.order.application.aftersale.cmd.AfterSaleCreateCmd;
import com.lemnt.order.application.aftersale.service.AfterSaleService;
import com.lemnt.order.application.order.executor.TradeOrderCloseCmdExe;
import com.lemnt.order.application.order.service.OrderPersistenceService;
import com.lemnt.order.application.paymentrefund.executor.PaymentRefundCreateCmdExe;
import com.lemnt.order.application.promotion.cmd.PromotionOrderCmd;
import com.lemnt.order.application.promotion.cmd.PromotionOrderLineCmd;
import com.lemnt.order.application.promotion.service.ActivityOrderLifecycleService;
import com.lemnt.order.application.promotion.service.PromotionOrderService;
import com.lemnt.order.client.bo.RefundStockBo;
import com.lemnt.order.domain.promotion.gateway.PromotionOrderGateway;
import com.lemnt.order.domain.promotion.model.PromotionOrder;
import com.lemnt.order.domain.settlement.model.SettlementOrder;
import com.lemnt.pay.application.payment.executor.PaymentNotifyCmdExe;
import com.lemnt.pay.application.payment.executor.TradePaymentCreateCmdExe;
import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundChannelGateway;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;
import com.lemnt.pay.infrastructure.payment.payment.PayChannelConfigResolver;
import com.lemnt.pay.infrastructure.paymentrefund.channel.PaymentRefundClient;
import com.lemnt.pay.infrastructure.paymentrefund.gateway.PaymentRefundChannelGatewayImpl;
import com.lemnt.product.domain.product.model.ProductSpu;
import com.lemnt.promotion.application.promotion.cmd.BargainActivityCreateCmd;
import com.lemnt.promotion.application.promotion.cmd.CombinationActivityCreateCmd;
import com.lemnt.promotion.application.promotion.cmd.CombinationProductCmd;
import com.lemnt.promotion.application.promotion.cmd.PointActivityCreateCmd;
import com.lemnt.promotion.application.promotion.cmd.PointProductCmd;
import com.lemnt.promotion.application.promotion.cmd.SeckillActivityCreateCmd;
import com.lemnt.promotion.application.promotion.cmd.SeckillConfigCreateCmd;
import com.lemnt.promotion.application.promotion.cmd.SeckillProductCmd;
import com.lemnt.promotion.application.promotion.service.BargainActivityManagementService;
import com.lemnt.promotion.application.promotion.service.CombinationActivityManagementService;
import com.lemnt.promotion.application.promotion.service.CombinationRecordService;
import com.lemnt.promotion.application.promotion.service.PointActivityManagementService;
import com.lemnt.promotion.application.promotion.service.SeckillActivityManagementService;
import com.lemnt.promotion.application.promotion.service.SeckillConfigManagementService;
import com.lemnt.promotion.domain.promotion.gateway.BargainRecordGateway;
import com.lemnt.promotion.domain.promotion.model.BargainRecord;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 活动订单映射、活动库存预占/恢复、拼团建团与砍价绑定的生产表事务验证。
 */
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
    AtomicLong memberPoints = new AtomicLong(10000);

    @BeforeEach
    void setup() throws Exception {
        f = new CatalogPersistenceTest();
        f.setup();
        f.points = memberPoints::get;
        var channel = Mockito.mock(PaymentRefundClient.class);
        Mockito.when(channel.channel()).thenReturn("ALIPAY");
        Mockito.when(
                channel.refund(
                    ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer(
                call -> {
                    PaymentRefund r = call.getArgument(0);
                    return new PaymentRefundResult(
                        r.refundNo(),
                        r.paymentNo(),
                        r.channel(),
                        r.amount(),
                        "SUCCESS",
                        "CHANNEL" + r.refundNo());
                });
        var payChannelConfigs = Mockito.mock(PayChannelConfigResolver.class);
        Mockito.when(payChannelConfigs.resolve(Mockito.any(), Mockito.any()))
            .thenReturn(
                new PayChannelConfig(
                    1L, 1L, "ALIPAY", 0, BigDecimal.ZERO, null, "{}", null));
        f.components.register(
            PaymentRefundChannelGateway.class,
            new PaymentRefundChannelGatewayImpl(List.of(channel), payChannelConfigs));
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
        seckillConfigId =
            seckillConfigs.create(
                new SeckillConfigCreateCmd("全天", "00:00:00", "23:59:59", List.of(), 0));
        var now = LocalDateTime.now();
        seckillActivityId =
            seckillActivities.create(
                new SeckillActivityCreateCmd(
                    spu.id(),
                    "秒杀",
                    null,
                    now.minusDays(1),
                    now.plusDays(1),
                    0,
                    List.of(seckillConfigId),
                    10,
                    5,
                    List.of(new SeckillProductCmd(skuId, new BigDecimal("5.00"), 5))));
        combinationActivityId =
            combinationActivities.create(
                new CombinationActivityCreateCmd(
                    "拼团",
                    spu.id(),
                    10,
                    5,
                    now.minusDays(1),
                    now.plusDays(1),
                    2,
                    false,
                    1,
                    List.of(new CombinationProductCmd(skuId, new BigDecimal("4.00")))));
        bargainActivityId =
            bargainActivities.create(
                new BargainActivityCreateCmd(
                    "砍价",
                    now.minusDays(1),
                    now.plusDays(1),
                    spu.id(),
                    skuId,
                    new BigDecimal("10.00"),
                    new BigDecimal("8.00"),
                    5,
                    1,
                    1,
                    1,
                    new BigDecimal("2.00"),
                    new BigDecimal("2.00")));
        pointActivityId =
            pointActivities.create(
                new PointActivityCreateCmd(
                    spu.id(),
                    null,
                    1,
                    List.of(
                        new PointProductCmd(
                            skuId, 5, 100, new BigDecimal("5.00"), 5))));
    }

    @AfterEach
    void cleanup() throws Exception {
        f.closeSchemaConnection();
    }

    record PromotionRefundResult(long refundPoints) {
    }

    void reserved(String no, SettlementOrder ignored) {
        f.components.get(ActivityOrderLifecycleService.class).reserved(no);
    }

    void paid(String no, SettlementOrder ignored) {
        var order = f.components.get(OrderPersistenceService.class).findByOrderNo(no);
        if ("PENDING_PAYMENT".equals(order.status()) && order.totalAmount().signum() > 0) {
            var p = f.components.get(TradePaymentCreateCmdExe.class).execute(no, "ALIPAY");
            f.components
                .get(PaymentNotifyCmdExe.class)
                .execute(
                    new VerifiedPaymentNotification(
                        p.paymentNo(),
                        no,
                        "ALIPAY",
                        "CH" + no,
                        order.totalAmount(),
                        "N" + no));
        }
        f.events.drainOrThrow();
    }

    void closed(String no) {
        f.components.get(TradeOrderCloseCmdExe.class).execute(no);
        f.events.drainOrThrow();
    }

    PromotionRefundResult refunded(
        String no, String request, SettlementOrder ignored, List<RefundStockBo> lines) {
        long points = 0;
        for (var input : lines) {
            var order = f.components.get(OrderPersistenceService.class).findByOrderNo(no);
            var item =
                order.items().stream()
                    .filter(v -> v.productId().equals(input.productId()))
                    .findFirst()
                    .orElseThrow();
            var service = f.components.get(AfterSaleService.class);
            var af =
                service.create(
                    new AfterSaleCreateCmd(
                        no,
                        request + ":" + input.productId(),
                        item.orderItemId(),
                        input.quantity(),
                        "REFUND_ONLY",
                        "活动退款"));
            service.agree(af.refundNo());
            f.components.get(PaymentRefundCreateCmdExe.class).execute(af.refundNo());
            f.events.drainOrThrow();
            points = Math.addExact(points, af.returnUsePoint());
        }
        return new PromotionRefundResult(points);
    }

    PromotionOrderCmd cmd(
        String type, Long activityId, Long recordId, int quantity, String requestNo) {
        return new PromotionOrderCmd(
            1L, type, activityId, recordId, null, skuId, quantity, null, requestNo);
    }

    @Test
    void seckillOrderReservesAndClosedRestoresStockOnce() {
        var order = orders.place(cmd("SECKILL", seckillActivityId, null, 2, "REQ_S1"));
        var mapping = mappings.findByOrderNo(order.orderNo());
        assertEquals("RESERVED", mapping.status());
        assertEquals(new BigDecimal("10.00"), order.totalAmount());
        reserved(order.orderNo(), order);
        reserved(order.orderNo(), order);
        assertEquals("RESERVED", mappings.findByOrderNo(order.orderNo()).status());
        assertEquals(3, seckillActivities.detail(seckillActivityId).stock());
        closed(order.orderNo());
        closed(order.orderNo());
        assertEquals("CLOSED", mappings.findByOrderNo(order.orderNo()).status());
        assertEquals(5, seckillActivities.detail(seckillActivityId).stock());
    }

    @Test
    void sameRequestReturnsSameOrderAndDifferentContentIsRejected() {
        var first = orders.place(cmd("SECKILL", seckillActivityId, null, 1, "REQ_IDEM"));
        var second = orders.place(cmd("SECKILL", seckillActivityId, null, 1, "REQ_IDEM"));
        assertEquals(first.orderNo(), second.orderNo());
        assertNotNull(mappings.findByRequest(1L, "REQ_IDEM").id());
        assertThrows(
            ServiceException.class,
            () -> orders.place(cmd("SECKILL", seckillActivityId, null, 2, "REQ_IDEM")));
    }

    @Test
    void combinationOrderCreatesRecordOnPaidOnce() {
        var order = orders.place(cmd("COMBINATION", combinationActivityId, null, 1, "REQ_C1"));
        reserved(order.orderNo(), order);
        paid(order.orderNo(), order);
        paid(order.orderNo(), order);
        var mapping = mappings.findByOrderNo(order.orderNo());
        assertEquals("PAID", mapping.status());
        assertNotNull(mapping.activityRecordId());
        assertEquals(
            1,
            f.components
                .get(CombinationRecordService.class)
                .byHead(mapping.activityRecordId())
                .size());
    }

    @Test
    void bargainOrderBindsRecordOnReservedAndClosedRestoresStock() {
        var record =
            bargainRecords.save(
                new BargainRecord(
                    null,
                    1L,
                    bargainActivityId,
                    spu.id(),
                    skuId,
                    new BigDecimal("10.00"),
                    new BigDecimal("8.00"),
                    new BigDecimal("8.00"),
                    2,
                    LocalDateTime.now(),
                    null));
        var order = orders.place(cmd("BARGAIN", null, record.id(), 1, "REQ_B1"));
        assertEquals(new BigDecimal("8.00"), order.totalAmount());
        reserved(order.orderNo(), order);
        assertEquals("RESERVED", mappings.findByOrderNo(order.orderNo()).status());
        assertNotNull(bargainRecords.find(record.id()).orderId());
        assertEquals(4, bargainActivities.detail(bargainActivityId).stock());
        closed(order.orderNo());
        assertEquals(5, bargainActivities.detail(bargainActivityId).stock());
    }

    @Test
    void pointOrderUsesPointsAndRefundRestoresStockByDelta() {
        var order = orders.place(cmd("POINT", pointActivityId, null, 2, "REQ_P1"));
        assertEquals(200L, order.usePoint());
        assertEquals(new BigDecimal("10.00"), order.totalAmount());
        reserved(order.orderNo(), order);
        paid(order.orderNo(), order);
        assertEquals(3, pointActivities.detail(pointActivityId).stock());
        refunded(order.orderNo(), "RF1", order, List.of(new RefundStockBo(skuId, 1)));
        refunded(order.orderNo(), "RF1", order, List.of(new RefundStockBo(skuId, 1)));
        assertEquals(4, pointActivities.detail(pointActivityId).stock());
        refunded(order.orderNo(), "RF2", order, List.of(new RefundStockBo(skuId, 1)));
        assertEquals(5, pointActivities.detail(pointActivityId).stock());
        var refunded =
            new SettlementOrder(
                order.id(),
                order.orderNo(),
                order.memberId(),
                order.totalAmount(),
                "REFUNDED",
                order.items(),
                order.createTime(),
                order.usePoint(),
                order.givePoint(),
                order.delivery());
        assertThrows(
            IllegalStateException.class,
            () ->
                refunded(
                    order.orderNo(),
                    "RF3",
                    refunded,
                    List.of(new RefundStockBo(skuId, 2))));
        assertEquals(5, pointActivities.detail(pointActivityId).stock());
        assertEquals("REFUNDED", mappings.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void zeroCashPointOrderUsesPointsAndInsufficientPointsIsRejected() {
        pointActivities.close(pointActivityId);
        var zero =
            pointActivities.create(
                new PointActivityCreateCmd(
                    spu.id(),
                    null,
                    1,
                    List.of(new PointProductCmd(skuId, 5, 100, BigDecimal.ZERO, 5))));
        var order = orders.place(cmd("POINT", zero, null, 1, "REQ_ZERO"));
        assertEquals(0, order.totalAmount().signum());
        assertEquals(100L, order.usePoint());
        reserved(order.orderNo(), order);
        paid(order.orderNo(), order);
        assertEquals("PAID", mappings.findByOrderNo(order.orderNo()).status());
        assertEquals(4, pointActivities.detail(zero).stock());
        var refunded =
            new SettlementOrder(
                order.id(),
                order.orderNo(),
                order.memberId(),
                order.totalAmount(),
                "REFUNDED",
                order.items(),
                order.createTime(),
                order.usePoint(),
                order.givePoint(),
                order.delivery());
        refunded(order.orderNo(), "RF-ZERO", refunded, List.of(new RefundStockBo(skuId, 1)));
        assertEquals(5, pointActivities.detail(zero).stock());
        assertEquals("REFUNDED", mappings.findByOrderNo(order.orderNo()).status());
        var alt = f.create("POINT_ALT", f.draft("BLUE", 10));
        f.productSpuGateway.shelf(alt.id(), true);
        var lowActivity =
            pointActivities.create(
                new PointActivityCreateCmd(
                    alt.id(),
                    null,
                    1,
                    List.of(
                        new PointProductCmd(
                            alt.skus().get(0).id(),
                            5,
                            100,
                            new BigDecimal("5.00"),
                            5))));
        memberPoints.set(50L);
        assertThrows(
            ServiceException.class,
            () ->
                orders.place(
                    new PromotionOrderCmd(
                        1L,
                        "POINT",
                        lowActivity,
                        null,
                        null,
                        alt.skus().get(0).id(),
                        2,
                        null,
                        "REQ_LOW")));
    }

    @Test
    void zeroCashPartialRefundReturnsPointsByQuantity() {
        pointActivities.close(pointActivityId);
        var zero =
            pointActivities.create(
                new PointActivityCreateCmd(
                    spu.id(),
                    null,
                    1,
                    List.of(new PointProductCmd(skuId, 5, 100, BigDecimal.ZERO, 5))));
        var order = orders.place(cmd("POINT", zero, null, 2, "REQ_ZPART"));
        assertEquals(200L, order.usePoint());
        reserved(order.orderNo(), order);
        paid(order.orderNo(), order);
        assertEquals(3, pointActivities.detail(zero).stock());
        var first = refunded(order.orderNo(), "RF-P1", order, List.of(new RefundStockBo(skuId, 1)));
        assertEquals(100L, first.refundPoints());
        assertEquals(4, pointActivities.detail(zero).stock());
        assertEquals("PAID", mappings.findByOrderNo(order.orderNo()).status());
        var refunded =
            new SettlementOrder(
                order.id(),
                order.orderNo(),
                order.memberId(),
                order.totalAmount(),
                "REFUNDED",
                order.items(),
                order.createTime(),
                order.usePoint(),
                order.givePoint(),
                order.delivery());
        var second =
            refunded(order.orderNo(), "RF-P2", refunded, List.of(new RefundStockBo(skuId, 1)));
        assertEquals(100L, second.refundPoints());
        assertEquals(5, pointActivities.detail(zero).stock());
        assertEquals("REFUNDED", mappings.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void zeroCashMultiLineOrderRefundsPointsByLine() {
        pointActivities.close(pointActivityId);
        var multi = f.create("POINT_MULTI", f.draft("MULTI_RED", 10), f.draft("MULTI_BLUE", 10));
        f.productSpuGateway.shelf(multi.id(), true);
        var first = multi.skus().get(0).id();
        var second = multi.skus().get(1).id();
        var activity =
            pointActivities.create(
                new PointActivityCreateCmd(
                    multi.id(),
                    null,
                    1,
                    List.of(
                        new PointProductCmd(first, 5, 100, BigDecimal.ZERO, 5),
                        new PointProductCmd(second, 5, 50, BigDecimal.ZERO, 5))));
        var cmd =
            PromotionOrderCmd.batch(
                1L,
                "POINT",
                activity,
                null,
                null,
                List.of(
                    new PromotionOrderLineCmd(first, 1),
                    new PromotionOrderLineCmd(second, 2)),
                null,
                "REQ_MULTI");
        var order = orders.place(cmd);
        assertEquals(200L, order.usePoint());
        assertEquals(0, order.totalAmount().signum());
        reserved(order.orderNo(), order);
        paid(order.orderNo(), order);
        assertEquals(7, pointActivities.detail(activity).stock());
        assertEquals(
            4,
            pointActivities.detail(activity).products().stream()
                .filter(item -> item.skuId().equals(first))
                .findFirst()
                .orElseThrow()
                .stock());
        assertEquals(
            3,
            pointActivities.detail(activity).products().stream()
                .filter(item -> item.skuId().equals(second))
                .findFirst()
                .orElseThrow()
                .stock());
        PromotionRefundResult result =
            refunded(order.orderNo(), "RF-M1", order, List.of(new RefundStockBo(first, 1)));
        assertEquals(100L, result.refundPoints());
        assertEquals(8, pointActivities.detail(activity).stock());
        assertEquals("PAID", mappings.findByOrderNo(order.orderNo()).status());
        var refundedOrder =
            new SettlementOrder(
                order.id(),
                order.orderNo(),
                order.memberId(),
                order.totalAmount(),
                "REFUNDED",
                order.items(),
                order.createTime(),
                order.usePoint(),
                order.givePoint(),
                order.delivery());
        result =
            refunded(
                order.orderNo(),
                "RF-M2",
                refundedOrder,
                List.of(new RefundStockBo(second, 2)));
        assertEquals(100L, result.refundPoints());
        assertEquals(10, pointActivities.detail(activity).stock());
        assertEquals("REFUNDED", mappings.findByOrderNo(order.orderNo()).status());
    }

    @Test
    void mappingRequestNumberIsUnique() {
        var mapping =
            new PromotionOrder(
                null,
                "TR-X",
                "REQ-U",
                1L,
                "SECKILL",
                seckillActivityId,
                null,
                null,
                skuId,
                1,
                BigDecimal.ONE,
                0L,
                0,
                "CREATED",
                null,
                null,
                null);
        mappings.save(mapping);
        assertThrows(
            DataIntegrityViolationException.class,
            () ->
                mappings.save(
                    new PromotionOrder(
                        null,
                        "TR-Y",
                        "REQ-U",
                        1L,
                        "SECKILL",
                        seckillActivityId,
                        null,
                        null,
                        skuId,
                        1,
                        BigDecimal.ONE,
                        0L,
                        0,
                        "CREATED",
                        null,
                        null,
                        null)));
    }
}
