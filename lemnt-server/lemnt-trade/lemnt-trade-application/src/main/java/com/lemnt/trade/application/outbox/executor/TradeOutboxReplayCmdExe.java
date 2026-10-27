package com.lemnt.trade.application.outbox.executor;

import com.lemnt.trade.domain.outbox.gateway.TradeOutboxGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 交易事务消息重放命令执行器。
 */
@Component
@RequiredArgsConstructor
public class TradeOutboxReplayCmdExe {
    private final TradeOutboxGateway tradeOutboxGateway;

    /**
     * 执行交易事务消息重放命令。
     */
    public boolean execute(String eventId) {
        if (eventId == null || eventId.isBlank() || eventId.length() > 64) {
            throw new IllegalArgumentException("事件编号不合法");
        }
        return tradeOutboxGateway.replayDead(eventId);
    }
}
