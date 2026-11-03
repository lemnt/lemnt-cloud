package com.lemnt.mall.application.dubbo;

import com.lemnt.mall.application.product.service.ProductInventoryService;
import com.lemnt.mall.application.promotion.service.PromotionOrderService;
import com.lemnt.mall.application.promotion.service.RewardGiftService;
import com.lemnt.mall.application.settlement.service.SettlementRecordService;
import com.lemnt.mall.client.CheckoutLifecycleClient;
import com.lemnt.mall.client.bo.RefundBenefitsBo;
import com.lemnt.mall.client.bo.RefundStockBo;
import com.lemnt.mall.domain.member.gateway.MemberTradeGateway;
import com.lemnt.mall.domain.member.model.MemberTradeChange;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 订单事件的商城本地事务，不参与交易库事务。
 */
@DubboService
@RequiredArgsConstructor
public class SettlementLifecycleClientImpl implements CheckoutLifecycleClient {
    private final MemberTradeGateway memberTradeGateway;
    private final SettlementRecordService settlementRecordService;
    private final OrderGateway orders;
    private final ProductInventoryService inventory;
    private final PromotionOrderService promotionOrders;
    private final RewardGiftService rewardGifts;

    /**
     * 由交易服务在下单接口中同步调用，失败时不继续预占库存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verify(String orderNo) {
        settlementRecordService.verifyCoupon(orderNo);
        var order = orders.findByOrderNo(orderNo);
        if (order == null
                || !Set.of("RESERVING", "PENDING_PAYMENT", "PAID", "SHIPPED", "COMPLETED")
                        .contains(order.status())) {
            throw new IllegalStateException("订单不能继续下单");
        }
        applyMember(order, "CREATE", null, BigDecimal.ZERO, 0L);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserved(String orderNo) {
        var order = orders.findByOrderNo(orderNo);
        if (order == null
                || !Set.of("PENDING_PAYMENT", "PAID", "SHIPPED", "COMPLETED", "CLOSED")
                        .contains(order.status())) {
            throw new IllegalStateException("订单尚未完成库存预占");
        }
        if (!"CLOSED".equals(order.status())) {
            settlementRecordService.consumeCart(orderNo, order.memberId());
        }
        promotionOrders.reserved(orderNo, order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paid(String orderNo) {
        var order = orders.findByOrderNo(orderNo);
        if (order == null || !Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status())) {
            throw new IllegalStateException("订单未支付");
        }
        settlementRecordService.useCoupon(orderNo);
        rewardGifts.issuePaid(order);
        applyMember(order, "PAID", null, BigDecimal.ZERO, 0L);
        promotionOrders.paid(orderNo, order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closed(String orderNo) {
        var order = orders.findByOrderNo(orderNo);
        if (order == null || !"CLOSED".equals(order.status())) {
            throw new IllegalStateException("订单未关闭");
        }
        // 库存和券都在商城库，任一补偿失败时回滚本地全部补偿写入。
        for (var item : order.items()) {
            inventory.cancelReservation(orderNo, item.productId(), item.quantity());
        }
        settlementRecordService.releaseCoupon(orderNo);
        applyMember(order, "CLOSED", null, BigDecimal.ZERO, 0L);
        promotionOrders.closed(orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refunded(RefundBenefitsBo plan) {
        if (plan == null
                || plan.ruleVersion() != 1
                || plan.quantity() < 1
                || plan.returnUsePoint() < 0
                || plan.deductGivePoint() < 0
                || plan.amount() == null
                || plan.amount().signum() < 0) {
            throw new IllegalArgumentException("冻结退款方案不合法");
        }
        var order = orders.findByOrderNo(plan.orderNo());
        if (order == null
                || order.items().stream()
                        .noneMatch(
                                i ->
                                        plan.orderItemId().equals(i.orderItemId())
                                                && plan.skuId().equals(i.productId())
                                                && plan.quantity() <= i.quantity())) {
            throw new IllegalArgumentException("冻结退款商品行不属于原订单");
        }
        applyRefund(
                plan.orderNo(),
                plan.afterSaleNo(),
                List.of(new RefundStockBo(plan.skuId(), plan.quantity())),
                plan.amount(),
                plan);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refunded(
            String orderNo, String refundNo, List<RefundStockBo> items, BigDecimal refundAmount) {
        applyRefund(orderNo, refundNo, items, refundAmount, null);
    }

    /** 库存去重及券状态属于mall本地事务，积分结果由新版退单明确给出。 */
    private void applyRefund(
            String orderNo,
            String refundNo,
            List<RefundStockBo> items,
            BigDecimal refundAmount,
            RefundBenefitsBo plan) {
        var order = orders.findByOrderNo(orderNo);
        if (order == null
                || !Set.of("PAID", "SHIPPED", "COMPLETED", "REFUNDED").contains(order.status())
                || items == null
                || items.isEmpty()
                || items.stream().map(RefundStockBo::productId).distinct().count()
                        != items.size()) {
            throw new IllegalArgumentException("售后回补的订单或商品不合法");
        }
        for (var item :
                items.stream()
                        .sorted(Comparator.comparing(RefundStockBo::productId))
                        .toList()) {
            if (order.items().stream()
                    .noneMatch(original -> original.productId().equals(item.productId()))) {
                throw new IllegalArgumentException("售后商品不属于原订单");
            }
            inventory.returnStock(orderNo, refundNo, item.productId(), item.quantity());
        }
        if ("REFUNDED".equals(order.status())) {
            settlementRecordService.returnUsedCoupon(orderNo);
        }
        if (plan == null) {
            var promotionRefund = promotionOrders.refunded(orderNo, refundNo, order, items);
            applyMember(
                    order,
                    "REFUND",
                    refundNo,
                    refundAmount,
                    promotionRefund == null ? 0L : promotionRefund.refundPoints());
        } else {
            promotionOrders.refunded(orderNo, refundNo, order, items, true);
            memberTradeGateway.apply(
                    new MemberTradeChange(
                            order.memberId(),
                            "REFUND",
                            orderNo,
                            refundNo,
                            order.usePoint(),
                            order.givePoint(),
                            order.totalAmount().movePointRight(2).longValueExact(),
                            refundAmount.movePointRight(2).longValueExact(),
                            "REFUNDED".equals(order.status()),
                            plan.returnUsePoint(),
                            plan.deductGivePoint(),
                            plan.ruleVersion()));
        }
    }

    /**
     * 只使用交易服务保存的原始积分与实付，禁止使用当前配置重算历史权益。
     */
    private void applyMember(
            SettlementOrder order,
            String operation,
            String refundNo,
            BigDecimal refundAmount,
            long refundPoint) {
        memberTradeGateway.apply(
                new MemberTradeChange(
                        order.memberId(),
                        operation,
                        order.orderNo(),
                        refundNo,
                        order.usePoint(),
                        order.givePoint(),
                        order.totalAmount().movePointRight(2).longValueExact(),
                        refundAmount.movePointRight(2).longValueExact(),
                        "REFUNDED".equals(order.status()),
                        refundPoint));
    }
}
