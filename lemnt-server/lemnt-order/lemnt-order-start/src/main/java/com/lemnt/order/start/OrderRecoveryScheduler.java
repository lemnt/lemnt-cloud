package com.lemnt.order.start;

import com.lemnt.order.application.order.service.OrderRecoveryService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 切流审核后启用，旧服务仍是写入方时不得并行恢复业务。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "lemnt.order.recovery.enabled", havingValue = "true")
public class OrderRecoveryScheduler {
    private final OrderRecoveryService service;

    @Scheduled(fixedDelayString = "${lemnt.order.recovery.interval-ms:5000}")
    public void execute() {
        service.execute();
    }
}
