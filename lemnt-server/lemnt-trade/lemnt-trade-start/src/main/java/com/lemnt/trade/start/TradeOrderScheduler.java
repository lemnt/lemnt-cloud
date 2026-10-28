package com.lemnt.trade.start;

import com.lemnt.trade.application.order.executor.TradeOrderCloseCmdExe;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Stream;

/** 订单超时关单任务，不负责消息投递或重试。 */
@Component
@ConditionalOnProperty(name = "lemnt.trade.scheduler.enabled", havingValue = "true")
public class TradeOrderScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(TradeOrderScheduler.class);
    private final TradeOrderCloseCmdExe tradeOrderCloseCmdExe;
    private final TradeOrderGateway tradeOrderGateway;

    /**
     * 创建订单超时关单调度器。
     */
    public TradeOrderScheduler(
            TradeOrderCloseCmdExe tradeOrderCloseCmdExe,
            TradeOrderGateway tradeOrderGateway) {
        this.tradeOrderCloseCmdExe = tradeOrderCloseCmdExe;
        this.tradeOrderGateway = tradeOrderGateway;
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
