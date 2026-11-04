package com.lemnt.product.start.event;

import com.lemnt.product.application.event.service.OutboxRelayService;
import com.lemnt.product.domain.event.gateway.EventPublishGateway;
import com.lemnt.product.domain.event.gateway.MessageOutboxGateway;
import com.lemnt.product.domain.event.model.OutboxMessage;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

/**
 * 传输失败不能确认成功；发送成功但CAS失败由租约恢复而不是新建事件。
 */
class EventRedeliveryTest {
    @Test
    void failedPublishRemainsRetryable() {
        var outbox = mock(MessageOutboxGateway.class);
        var publisher = mock(EventPublishGateway.class);
        var message = new OutboxMessage(1, "E1", "STOCK_PRE_RESERVED", "O1:7", "{}", 1, 1);
        when(outbox.claim(100)).thenReturn(List.of(message));
        doThrow(new IllegalStateException("消息服务不可用")).when(publisher).publish(message);
        new OutboxRelayService(outbox, publisher).relay();
        verify(outbox).failed(message, "IllegalStateException");
        verify(outbox, never()).sent(any());
    }

    @Test
    void lostCompletionLeaseDoesNotCreateReplacementIdentity() {
        var outbox = mock(MessageOutboxGateway.class);
        var publisher = mock(EventPublishGateway.class);
        var message = new OutboxMessage(1, "E1", "STOCK_PRE_RESERVED", "O1:7", "{}", 1, 1);
        when(outbox.claim(100)).thenReturn(List.of(message));
        new OutboxRelayService(outbox, publisher).relay();
        verify(publisher).publish(message);
        verify(outbox).sent(message);
        verify(outbox, never()).failed(any(), any());
        verify(outbox, never()).enqueue(any(), any(), any(), any());
    }
}
