package com.lemnt.trade.application.order.service;

import com.lemnt.trade.domain.order.model.TradeOperator;


import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 结算生命周期服务。
 */
@Component
@RequiredArgsConstructor
public class OrderLifecycleService {
    private final InventoryConfirmationGateway inventoryConfirmationGateway;
    private final OrderPersistenceService orderPersistenceService;
    private final TradeOrderGateway tradeOrderGateway;
    private final OrderLogService logs;

    /**
     * 库存预留成功。
     */
    @Transactional(rollbackFor = Exception.class)
    public void reservationSucceeded(String orderNo) {
        var order = orderPersistenceService.findByOrderNoForUpdate(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!"RESERVING".equals(order.status())) {
            return ;
        }
        if (!tradeOrderGateway.transitionStatus(orderNo, "RESERVING", "PENDING_PAYMENT")) {
            throw new IllegalStateException("订单状态冲突");
        }
    }

    /**
     * 库存预留失败。
     */
    @Transactional(rollbackFor = Exception.class)
    public void reservationFailed(String orderNo) {
        var order = orderPersistenceService.findByOrderNoForUpdate(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!"RESERVING".equals(order.status())) {
            return ;
        }
        if (!tradeOrderGateway.transitionStatus(orderNo, "RESERVING", "CLOSED")) {
            throw new IllegalStateException("订单状态冲突");
        }
        logs.record(order, "RESERVING", "CLOSED", 41, TradeOperator.system(), "库存预占失败，系统取消订单");
    }

    /**
     * 库存已确认。
     */
    @Transactional(rollbackFor = Exception.class)
    public void inventoryConfirmed(String orderNo) {
        var order = orderPersistenceService.findByOrderNoForUpdate(orderNo);
        if (order == null
                || !Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status())) {
            throw new IllegalStateException("订单状态不允许确认库存");
        }
        inventoryConfirmationGateway.confirmInventory(orderNo, tradeOrderGateway.databaseNow());
    }
}
