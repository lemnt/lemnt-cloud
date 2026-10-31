package com.lemnt.trade.start;

import com.lemnt.trade.application.payment.service.PayNotifyTaskService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 支付结果派发任务重试调度，逐条隔离失败，不在此实现渠道调用。 */
@Component
@ConditionalOnProperty(name = "lemnt.trade.pay-notify.enabled", havingValue = "true")
public class PayNotifyScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(PayNotifyScheduler.class);
    private static final int BATCH = 50;

    private final PayNotifyTaskService service;

    public PayNotifyScheduler(PayNotifyTaskService service) {
        this.service = service;
    }

    /** 扫描到期任务并重试，单条失败不影响本批其余任务。 */
    @Scheduled(fixedDelayString = "${lemnt.trade.pay-notify.delay-ms:60000}")
    public void retry() {
        try {
            service.processDue(BATCH);
        } catch (RuntimeException error) {
            LOG.error("Failed to process pay notify tasks", error);
        }
    }
}
