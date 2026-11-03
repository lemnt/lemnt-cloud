package com.lemnt.product.start;

import com.lemnt.product.application.event.service.OutboxRelayService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 恢复本库事务消息；仅在该服务作为唯一业务写入方启用后运行。 */
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {
    private final OutboxRelayService relay;

    @Scheduled(fixedDelayString = "${commerce.outbox.interval-ms:5000}")
    public void execute() {
        relay.relay();
    }
}
