package com.lemnt.trade.application.outbox.executor;

import com.lemnt.trade.application.order.service.OrderLifecycleService;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.inventory.gateway.InventoryGateway;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.outbox.gateway.TradeOutboxGateway;
import com.lemnt.trade.domain.outbox.model.OutboxEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

/**
 * At-least-once outbox processor. All inventory methods MUST be idempotent. Future MQ integration
 * can publish these events and move inventory handling to a consumer.
 */
@Component
@RequiredArgsConstructor
public class TradeOutboxDispatchCmdExe {
    private final OrderPersistenceService orderPersistenceService;
    private static final Logger LOG = LoggerFactory.getLogger(TradeOutboxDispatchCmdExe.class);
    private final TradeOutboxGateway tradeOutboxGateway;
    private final InventoryGateway productCatalogGateway;
    private final com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway benefits;
    private final OrderLifecycleService orderLifecycleService;

    /**
     * 执行交易事务消息派发。
     */
    public int execute(int batchSize) {
        int processed = 0;
        for (OutboxEvent event : tradeOutboxGateway.pending(batchSize)) {
            String token = UUID.randomUUID().toString();
            if (!tradeOutboxGateway.claim(event.eventId(), token)) {
                continue;
            }
            try {
                process(event);
                if (tradeOutboxGateway.delivered(event.eventId(), token)) {
                    processed++;
                }
            } catch (Exception ex) {
                tradeOutboxGateway.failed(event.eventId(), token, event.retryCount() + 1);
                LOG.error(
                        "Outbox delivery failed, eventId={}, orderNo={}",
                        event.eventId(),
                        event.aggregateNo(),
                        ex);
            }
        }
        return processed;
    }

    private void process(OutboxEvent event) {
        TradeOrder order = orderPersistenceService.findByOrderNo(event.aggregateNo());
        if (order == null) {
            throw new IllegalStateException("Order not found: " + event.aggregateNo());
        }
        switch (event.eventType()) {
            case "TRADE_ORDER_RESERVE" -> {
                if (!"RESERVING".equals(order.status())) {
                    if (Set.of("PENDING_PAYMENT", "PAID", "SHIPPED", "COMPLETED").contains(order.status())) {
                        benefits.reserved(order.orderNo());
                    }
                    return;
                }
                if (order.items().isEmpty()) {
                    throw new IllegalStateException("预占订单没有明细");
                }
                for (var item : order.items()) {
                    if (!productCatalogGateway.reserveStock(order.orderNo(), item.productId(), item.quantity())) {
                        orderLifecycleService.reservationFailed(order.orderNo());
                        return ;
                    }
                }
                orderLifecycleService.reservationSucceeded(order.orderNo());
                benefits.reserved(order.orderNo());
            }
            case "TRADE_ORDER_PAID" -> {
                if (!Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status())) {
                    throw new IllegalStateException("Order not paid");
                }
                if (order.items().isEmpty()) {
                    throw new IllegalStateException("已支付订单没有明细");
                }
                order.items()
                        .forEach(item -> productCatalogGateway.confirmStock(order.orderNo(), item.productId()));
                orderLifecycleService.inventoryConfirmed(order.orderNo());
                benefits.paid(order.orderNo());
            }
            case "TRADE_ORDER_CLOSED" -> {
                if (!"CLOSED".equals(order.status())) {
                    throw new IllegalStateException("Order not closed");
                }
                order.items()
                        .forEach(
                        item ->
                        productCatalogGateway.cancelReservation(
                        order.orderNo(),
                        item.productId(),
                        item.quantity()));
                benefits.closed(order.orderNo());
            }
            default ->
            throw new IllegalArgumentException(
                    "Unsupported outbox event: " + event.eventType());
        }
    }
}
