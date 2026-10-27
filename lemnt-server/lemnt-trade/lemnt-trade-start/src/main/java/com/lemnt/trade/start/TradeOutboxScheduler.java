package com.lemnt.trade.start;

import com.lemnt.trade.application.order.executor.TradeOrderCloseCmdExe;
import com.lemnt.trade.application.outbox.executor.TradeOutboxDispatchCmdExe;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Stream;

/**
 * Enable only on designated worker nodes; multi-instance arbitration is done by outbox claim. A
 * production payment channel must confirm expiry and absence of successful payments before closing.
 */
@Component
@ConditionalOnProperty(name = "lemnt.trade.scheduler.enabled", havingValue = "true")
public class TradeOutboxScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(TradeOutboxScheduler.class);
    private final TradeOutboxDispatchCmdExe tradeOutboxDispatchExe;
    private final TradeOrderCloseCmdExe tradeOrderCloseCmdExe;
    private final TradeOrderGateway tradeOrderGateway;

    /**
     * 创建交易事务消息调度器实例。
     */
    public TradeOutboxScheduler(
            TradeOutboxDispatchCmdExe tradeOutboxDispatchExe,
            TradeOrderCloseCmdExe tradeOrderCloseCmdExe,
            TradeOrderGateway tradeOrderGateway) {
        this.tradeOutboxDispatchExe = tradeOutboxDispatchExe;
        this.tradeOrderCloseCmdExe = tradeOrderCloseCmdExe;
        this.tradeOrderGateway = tradeOrderGateway;
    }

    /**
     * 派发。
     */
    @Scheduled(fixedDelayString = "${lemnt.trade.scheduler.outbox-delay-ms:10000}")
    public void dispatch() {
        tradeOutboxDispatchExe.execute(50);
    }

    /**
     * 关闭已过期记录。
     */
    @Scheduled(fixedDelayString = "${lemnt.trade.scheduler.close-delay-ms:60000}")
    public void closeExpired() {
        LocalDateTime cutoff = tradeOrderGateway.databaseNow().minusMinutes(30);
        for (var order :
                Stream.concat(
                tradeOrderGateway.findReservingCreatedBefore(cutoff, 50).stream(),
                tradeOrderGateway.findPendingPaymentCreatedBefore(cutoff, 50).stream())
                .toList()) {
            try {
                tradeOrderCloseCmdExe.execute(order.orderNo());
            } catch (RuntimeException ex) {
                LOG.error("Failed to close expired trade order {}", order.orderNo(), ex);
            }
        }
    }
}
