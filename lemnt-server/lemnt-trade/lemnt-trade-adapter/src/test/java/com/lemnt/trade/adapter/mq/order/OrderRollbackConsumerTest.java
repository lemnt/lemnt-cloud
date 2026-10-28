package com.lemnt.trade.adapter.mq.order;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.trade.application.order.executor.OrderRollbackCmdExe;
import com.lemnt.trade.domain.order.model.OrderRollbackEvent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OrderRollbackConsumerTest {
    private final OrderRollbackCmdExe executor = mock(OrderRollbackCmdExe.class);
    private final OrderRollbackConsumer consumer = new OrderRollbackConsumer(executor);

    @Test
    void realMessageCodecRoutesOrderToUnifiedCompensation() {
        consumer.onMessage(JsonUtils.toJsonStr(new OrderRollbackEvent("TR1")));
        verify(executor).execute("TR1");
    }

    @Test
    void remoteFailureEscapesToMqContainerForRetry() {
        doThrow(new IllegalStateException("商城补偿失败")).when(executor).execute("TR1");
        assertThrows(IllegalStateException.class,
                () -> consumer.onMessage(JsonUtils.toJsonStr(new OrderRollbackEvent("TR1"))));
    }

    @Test
    void missingOrderCannotBeAcknowledgedAsSuccessfulCompensation() {
        assertThrows(IllegalArgumentException.class, () -> consumer.onMessage("{}"));
        verifyNoInteractions(executor);
    }
}
