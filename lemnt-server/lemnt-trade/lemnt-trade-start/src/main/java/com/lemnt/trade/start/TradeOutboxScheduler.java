package com.lemnt.trade.start;

import com.lemnt.trade.application.executor.TradeOutboxDispatchExe;
import com.lemnt.trade.application.executor.TradeOrderCloseCmdExe;
import com.lemnt.trade.domain.gateway.TradeOrderGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * Enable only on designated worker nodes; multi-instance arbitration is done by outbox claim. A
 * production payment channel must confirm expiry and absence of successful payments before closing.
 */
@Component
@ConditionalOnProperty(name = "lemnt.trade.scheduler.enabled", havingValue = "true")
public class TradeOutboxScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(TradeOutboxScheduler.class);
    private final TradeOutboxDispatchExe dispatcher;
    private final TradeOrderCloseCmdExe closer;
    private final TradeOrderGateway orders;

    public TradeOutboxScheduler(
            TradeOutboxDispatchExe dispatcher,
            TradeOrderCloseCmdExe closer,
            TradeOrderGateway orders) {
        this.dispatcher = dispatcher;
        this.closer = closer;
        this.orders = orders;
    }

    @Scheduled(fixedDelayString = "${lemnt.trade.scheduler.outbox-delay-ms:10000}")
    public void dispatch() {
        dispatcher.execute(50);
    }

    @Scheduled(fixedDelayString = "${lemnt.trade.scheduler.close-delay-ms:60000}")
    public void closeExpired() {
        LocalDateTime cutoff = orders.databaseNow().minusMinutes(30);
        for (var order :
                java.util.stream.Stream.concat(
                                orders.findReservingCreatedBefore(cutoff, 50).stream(),
                                orders.findPendingPaymentCreatedBefore(cutoff, 50).stream())
                        .toList()) {
            try {
                closer.execute(order.orderNo());
            } catch (RuntimeException ex) {
                LOG.error("Failed to close expired trade order {}", order.orderNo(), ex);
            }
        }
    }
}
