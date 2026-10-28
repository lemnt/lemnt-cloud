package com.lemnt.mall.application.dubbo;

import com.lemnt.mall.application.product.service.ProductInventoryService;
import com.lemnt.mall.application.settlement.service.SettlementRecordService;
import com.lemnt.mall.client.CheckoutLifecycleClient;
import com.lemnt.mall.client.bo.RefundStockBo;
import com.lemnt.mall.domain.member.gateway.MemberTradeGateway;
import com.lemnt.mall.domain.member.model.MemberTradeChange;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
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

    /**
     * 由交易服务在下单接口中同步调用，失败时不继续预占库存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verify(String orderNo) {
        settlementRecordService.verifyCoupon(orderNo);
        var order = orders.findByOrderNo(orderNo);
        if (order == null || !Set.of("RESERVING", "PENDING_PAYMENT", "PAID", "SHIPPED", "COMPLETED")
                .contains(order.status())) {
            throw new IllegalStateException("订单不能继续下单");
        }
        applyMember(order, "CREATE", null, BigDecimal.ZERO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserved(String orderNo) {
        var order = orders.findByOrderNo(orderNo);
        if (order == null || !Set.of("PENDING_PAYMENT", "PAID", "SHIPPED", "COMPLETED", "CLOSED")
                .contains(order.status())) {
            throw new IllegalStateException("订单尚未完成库存预占");
        }
        if (!"CLOSED".equals(order.status())) {
            settlementRecordService.consumeCart(orderNo, order.memberId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paid(String orderNo) {
        var order = orders.findByOrderNo(orderNo);
        if (order == null || !Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status())) {
            throw new IllegalStateException("订单未支付");
        }
        settlementRecordService.useCoupon(orderNo);
        applyMember(order, "PAID", null, BigDecimal.ZERO);
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
        applyMember(order, "CLOSED", null, BigDecimal.ZERO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refunded(String orderNo, String refundNo, List<RefundStockBo> items, BigDecimal refundAmount) {
        var order = orders.findByOrderNo(orderNo);
        if (order == null || !Set.of("PAID", "SHIPPED", "COMPLETED", "REFUNDED").contains(order.status())
                || items == null || items.isEmpty()
                || items.stream().map(RefundStockBo::productId).distinct().count() != items.size()) {
            throw new IllegalArgumentException("售后回补的订单或商品不合法");
        }
        for (var item : items.stream().sorted(java.util.Comparator.comparing(
                RefundStockBo::productId)).toList()) {
            if (order.items().stream().noneMatch(original -> original.productId().equals(item.productId()))) {
                throw new IllegalArgumentException("售后商品不属于原订单");
            }
            inventory.returnStock(orderNo, refundNo, item.productId(), item.quantity());
        }
        if ("REFUNDED".equals(order.status())) {
            settlementRecordService.returnUsedCoupon(orderNo);
        }
        applyMember(order, "REFUND", refundNo, refundAmount);
    }

    /**
     * 只使用交易服务保存的原始积分与实付，禁止使用当前配置重算历史权益。
     */
    private void applyMember(SettlementOrder order, String operation,
                             String refundNo, BigDecimal refundAmount) {
        memberTradeGateway.apply(new MemberTradeChange(order.memberId(),
                operation, order.orderNo(), refundNo, order.usePoint(), order.givePoint(),
                order.totalAmount().movePointRight(2).longValueExact(),
                refundAmount.movePointRight(2).longValueExact(), "REFUNDED".equals(order.status())));
    }
}
