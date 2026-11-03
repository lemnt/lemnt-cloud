package com.lemnt.order.start;

import com.lemnt.order.application.brokerage.service.BrokerageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 到期佣金解冻任务，单条失败由下一次调度继续重试。 */
@Component
@ConditionalOnProperty(name = "lemnt.trade.scheduler.enabled", havingValue = "true")
public class BrokerageUnfreezeScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(BrokerageUnfreezeScheduler.class);
    private final BrokerageService service;

    public BrokerageUnfreezeScheduler(BrokerageService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${lemnt.trade.brokerage.unfreeze-delay-ms:60000}")
    public void unfreeze() {
        try {
            service.unfreezeDue(50);
        } catch (RuntimeException error) {
            LOG.error("Failed to unfreeze brokerage records", error);
        }
    }
}
