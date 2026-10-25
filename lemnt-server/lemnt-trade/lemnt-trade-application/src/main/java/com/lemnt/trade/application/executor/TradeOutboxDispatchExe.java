package com.lemnt.trade.application.executor;

import com.lemnt.trade.domain.gateway.ProductCatalogGateway;
import com.lemnt.trade.domain.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.gateway.TradeOutboxGateway;
import com.lemnt.trade.domain.model.OutboxEvent;
import com.lemnt.trade.domain.model.TradeOrder;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * At-least-once outbox processor. All inventory methods MUST be idempotent. Future MQ integration
 * can publish these events and move inventory handling to a consumer.
 */
@Component
@RequiredArgsConstructor
public class TradeOutboxDispatchExe {
    private static final Logger LOG = LoggerFactory.getLogger(TradeOutboxDispatchExe.class);
    private final TradeOutboxGateway outbox;
    private final TradeOrderGateway orders;
    private final ProductCatalogGateway catalog;
    private final com.lemnt.trade.application.checkout.service.CheckoutLifecycleService lifecycle;

    public int execute(int batchSize) {
        int processed = 0;
        for (OutboxEvent event : outbox.pending(batchSize)) {
            String token = UUID.randomUUID().toString();
            if (!outbox.claim(event.eventId(), token)) continue;
            try {
                process(event);
                if (outbox.delivered(event.eventId(), token)) processed++;
            } catch (Exception ex) {
                outbox.failed(event.eventId(), token, event.retryCount() + 1);
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
        TradeOrder order = orders.findByOrderNo(event.aggregateNo());
        if (order == null)
            throw new IllegalStateException("Order not found: " + event.aggregateNo());
        switch (event.eventType()) {
            case "TRADE_ORDER_RESERVE" -> {
                if (!"RESERVING".equals(order.status())) return;
                if (order.items().isEmpty()) throw new IllegalStateException("预占订单没有明细");
                for (var item : order.items()) {
                    if (!catalog.reserveStock(order.orderNo(), item.productId(), item.quantity())) {
                        lifecycle.reservationFailed(order.orderNo());
                        return;
                    }
                }
                lifecycle.reservationSucceeded(order.orderNo());
            }
            case "TRADE_ORDER_PAID" -> {
                if (!java.util.Set.of("PAID", "SHIPPED", "COMPLETED").contains(order.status()))
                    throw new IllegalStateException("Order not paid");
                if (order.items().isEmpty()) throw new IllegalStateException("已支付订单没有明细");
                order.items()
                        .forEach(item -> catalog.confirmStock(order.orderNo(), item.productId()));
                lifecycle.inventoryConfirmed(order.orderNo());
            }
            case "TRADE_ORDER_CLOSED" -> {
                if (!"CLOSED".equals(order.status()))
                    throw new IllegalStateException("Order not closed");
                order.items()
                        .forEach(
                                item ->
                                        catalog.cancelReservation(
                                                order.orderNo(),
                                                item.productId(),
                                                item.quantity()));
            }
            default ->
                    throw new IllegalArgumentException(
                            "Unsupported outbox event: " + event.eventType());
        }
    }
}
