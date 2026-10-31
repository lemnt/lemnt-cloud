package com.lemnt.trade.application.order.service;

import com.lemnt.trade.domain.inventory.gateway.InventoryGateway;
import com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway;
import com.lemnt.trade.domain.order.gateway.OrderRollbackGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/** 本地订单事务结束后同步调用商城，只有库存与券回滚异步发送。 */
@Component
@RequiredArgsConstructor
public class OrderRemoteService {
    private final OrderPersistenceService orders;
    private final InventoryGateway inventory;
    private final OrderBenefitsGateway benefits;
    private final OrderLifecycleService lifecycle;
    private final OrderRollbackGateway rollback;

    /** 同步预占全部库存，原订单重试不会重复扣减。 */
    public void reserve(String orderNo) {
        var order = orders.findByOrderNo(orderNo);
        if (order == null || order.items().isEmpty()) {
            throw new IllegalStateException("订单或订单明细不存在");
        }
        if ("REFUNDED".equals(order.status())) {
            return;
        }
        if ("CLOSED".equals(order.status())) {
            closed(orderNo);
            throw new IllegalStateException("订单已关闭，请查询原订单结果");
        }
        benefits.verify(orderNo);
        if ("RESERVING".equals(order.status())) {
            for (var item : order.items()) {
                if (!inventory.reserveStock(orderNo, item.productId(), item.quantity())) {
                    lifecycle.reservationFailed(orderNo);
                    closed(orderNo);
                    throw new IllegalStateException("预占库存失败，已提交库存与券回滚补偿");
                }
            }
            lifecycle.reservationSucceeded(orderNo);
        }
        var current = orders.findByOrderNo(orderNo);
        if (current == null || !Set.of("PENDING_PAYMENT", "PAID", "SHIPPED", "COMPLETED")
                .contains(current.status())) {
            if (current != null && "CLOSED".equals(current.status())) {
                closed(orderNo);
            }
            throw new IllegalStateException("订单状态已变化，不能完成下单");
        }
        try {
            benefits.reserved(orderNo);
        } catch (RuntimeException exception) {
            var latest = orders.findByOrderNo(orderNo);
            if (latest != null && "PENDING_PAYMENT".equals(latest.status())) {
                lifecycle.reservationCallbackFailed(orderNo);
                closed(orderNo);
            }
            throw exception;
        }
        var completed = orders.findByOrderNo(orderNo);
        if (completed != null && completed.totalAmount().signum() == 0) {
            zeroPay(orderNo);
        }
    }

    /** 零元订单在预占成功后直接支付并确认库存；重复调用保持幂等。 */
    public void zeroPay(String orderNo) {
        var order = orders.findByOrderNo(orderNo);
        if (order == null || order.totalAmount().signum() != 0) {
            return;
        }
        if ("PENDING_PAYMENT".equals(order.status())) {
            lifecycle.zeroPay(orderNo);
        }
        var current = orders.findByOrderNo(orderNo);
        if (current != null && "PAID".equals(current.status())) {
            paid(orderNo);
        }
    }

    /** 已支付状态先提交，再同步确认库存及核券；调用失败由原支付通知重试。 */
    public void paid(String orderNo) {
        var order = orders.findByOrderNo(orderNo);
        // 已退款订单重放原支付通知时，不再次确认库存和核销优惠券。
        if (order != null && "REFUNDED".equals(order.status())) {
            return;
        }
        if (order == null || !Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status())
                || order.items().isEmpty()) {
            throw new IllegalStateException("订单尚未支付或没有明细");
        }
        for (var item : order.items()) {
            inventory.confirmStock(orderNo, item.productId());
        }
        benefits.paid(orderNo);
        lifecycle.inventoryConfirmed(orderNo);
    }

    /** 关单后异步发送库存与优惠券的统一补偿消息。 */
    public void closed(String orderNo) {
        var order = orders.findByOrderNo(orderNo);
        if (order == null || !"CLOSED".equals(order.status())) {
            throw new IllegalStateException("订单未关闭");
        }
        rollback.publish(orderNo);
    }
}
