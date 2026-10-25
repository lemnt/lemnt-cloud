package com.lemnt.trade.application.executor;

import com.lemnt.trade.domain.gateway.ProductCatalogGateway;
import com.lemnt.trade.domain.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.gateway.TradeOutboxGateway;
import com.lemnt.trade.domain.model.OutboxEvent;
import com.lemnt.trade.domain.model.TradeOrder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * At-least-once outbox processor. All inventory methods MUST be idempotent.
 * Future MQ integration can publish these events and move inventory handling to a consumer.
 */
@Component
@RequiredArgsConstructor
public class TradeOutboxDispatchExe {
    private static final Logger LOG = LoggerFactory.getLogger(TradeOutboxDispatchExe.class);
    private final TradeOutboxGateway outbox;
    private final TradeOrderGateway orders;
    private final ProductCatalogGateway catalog;

    public int execute(int batchSize) {
        int processed = 0;
        for (OutboxEvent event : outbox.pending(batchSize)) {
            if (!outbox.claim(event.eventId())) continue;
            try {
                process(event);
                outbox.delivered(event.eventId());
                processed++;
            } catch (Exception ex) {
                outbox.failed(event.eventId());
                LOG.error("Outbox delivery failed, eventId={}, orderNo={}",
                        event.eventId(), event.aggregateNo(), ex);
            }
        }
        return processed;
    }

    private void process(OutboxEvent event) {
        TradeOrder order = orders.findByOrderNo(event.aggregateNo());
        if (order == null) throw new IllegalStateException("Order not found: " + event.aggregateNo());
        switch (event.eventType()) {
            case "TRADE_ORDER_PAID" -> {
                if (!"PAID".equals(order.status())) throw new IllegalStateException("Order not paid");
                order.items().forEach(item -> catalog.confirmStock(order.orderNo(), item.productId()));
            }
            case "TRADE_ORDER_CLOSED" -> {
                if (!"CLOSED".equals(order.status())) throw new IllegalStateException("Order not closed");
                order.items().forEach(item -> catalog.releaseStock(order.orderNo(), item.productId()));
            }
            default -> throw new IllegalArgumentException("Unsupported outbox event: " + event.eventType());
        }
    }
}
