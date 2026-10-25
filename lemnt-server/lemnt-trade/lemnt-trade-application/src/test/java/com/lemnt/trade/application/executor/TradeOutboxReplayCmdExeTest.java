package com.lemnt.trade.application.executor;

import com.lemnt.trade.domain.gateway.TradeOutboxGateway;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TradeOutboxReplayCmdExeTest {
    final TradeOutboxGateway gateway = mock(TradeOutboxGateway.class);
    final TradeOutboxReplayCmdExe replay = new TradeOutboxReplayCmdExe(gateway);

    @Test
    void rejectsInvalidIdentifiersBeforePersistence() {
        assertThrows(IllegalArgumentException.class, () -> replay.execute(null));
        assertThrows(IllegalArgumentException.class, () -> replay.execute(" "));
        assertThrows(IllegalArgumentException.class, () -> replay.execute("E".repeat(65)));
        verifyNoInteractions(gateway);
    }

    @Test
    void returnsActualConditionalReplayOutcome() {
        when(gateway.replayDead("dead")).thenReturn(true);
        assertTrue(replay.execute("dead"));
        assertFalse(replay.execute("delivered"));
    }
}
