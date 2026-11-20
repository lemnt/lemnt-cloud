package com.lemnt.pay.start;

import com.lemnt.pay.application.payment.service.PaymentRecoveryService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 独立检查渠道待确认款项，复用已提交资金事实的通知任务。 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "lemnt.trade.payment-recovery.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentRecoveryScheduler {
    private final PaymentRecoveryService service;

    /** 查找渠道已支付但回调未到达的订单。 */
    @Scheduled(fixedDelayString = "${lemnt.trade.payment-recovery.delay-ms:60000}")
    public void syncPayments() {
        service.syncPayments();
    }

    /** 查询渠道仍待确认的原退款单。 */
    @Scheduled(fixedDelayString = "${lemnt.trade.payment-recovery.delay-ms:60000}")
    public void syncRefunds() {
        service.syncRefunds();
    }
}
