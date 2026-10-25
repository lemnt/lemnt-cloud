package com.lemnt.trade.application.executor;

import com.lemnt.trade.domain.gateway.TradeOutboxGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeOutboxReplayCmdExe {
    private final TradeOutboxGateway outbox;

    public boolean execute(String eventId) {
        if (eventId == null || eventId.isBlank() || eventId.length() > 64) {
            throw new IllegalArgumentException("事件编号不合法");
        }
        return outbox.replayDead(eventId);
    }
}
