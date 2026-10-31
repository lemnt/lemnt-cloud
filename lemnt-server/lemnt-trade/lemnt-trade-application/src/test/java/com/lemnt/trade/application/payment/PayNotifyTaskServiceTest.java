package com.lemnt.trade.application.payment;

import com.lemnt.trade.application.payment.service.PayNotifyDispatcher;
import com.lemnt.trade.application.payment.service.PayNotifyTaskService;
import com.lemnt.trade.domain.payment.gateway.PayNotifyTaskGateway;
import com.lemnt.trade.domain.payment.model.PayNotifyTask;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 派发任务入队幂等、CAS 抢占、失败退避与人工重发。 */
class PayNotifyTaskServiceTest {
    final PayNotifyTaskGateway tasks = mock(PayNotifyTaskGateway.class);
    final PayNotifyDispatcher dispatcher = mock(PayNotifyDispatcher.class);
    final PayNotifyTaskService service = new PayNotifyTaskService(tasks, dispatcher);
    final LocalDateTime now = LocalDateTime.of(2026, 1, 1, 0, 0);

    private PayNotifyTask task(long id, int times, String status) {
        return new PayNotifyTask(id, "PAY", "P" + id, "TR" + id, status, times, 8, null, null, null, null);
    }

    @Test
    void enqueueIsIdempotentAndValidatesType() {
        when(tasks.createIfAbsent(any())).thenReturn(task(1, 0, "PENDING"));
        assertEquals(1L, service.enqueue("PAY", "P1", "TR1").id());
        ArgumentCaptor<PayNotifyTask> captor = ArgumentCaptor.forClass(PayNotifyTask.class);
        verify(tasks).createIfAbsent(captor.capture());
        assertEquals("PENDING", captor.getValue().status());
        assertEquals(0, captor.getValue().notifyTimes());
        assertEquals(8, captor.getValue().maxNotifyTimes());
        assertThrows(IllegalArgumentException.class, () -> service.enqueue("OTHER", "P1", "TR1"));
        assertThrows(IllegalArgumentException.class, () -> service.enqueue("PAY", "", "TR1"));
    }

    @Test
    void attemptSuccessMarksTaskAndLogsOnce() {
        when(tasks.find(1L)).thenReturn(task(1, 0, "PENDING"));
        when(tasks.claim(1L, 0)).thenReturn(true);
        when(tasks.databaseNow()).thenReturn(now);
        when(tasks.finishSuccess(1L, 1, "OK", now)).thenReturn(true);
        assertTrue(service.attempt(1L));
        verify(dispatcher).dispatch(any());
        verify(tasks).finishSuccess(1L, 1, "OK", now);
        verify(tasks).appendLog(argThat(log -> log.notifyTimes() == 1 && "SUCCESS".equals(log.status())));
    }

    @Test
    void attemptFailureSchedulesNextAttemptAndReturnsFalse() {
        when(tasks.find(1L)).thenReturn(task(1, 0, "PENDING"));
        when(tasks.claim(1L, 0)).thenReturn(true);
        when(tasks.databaseNow()).thenReturn(now);
        doThrow(new IllegalStateException("核券失败")).when(dispatcher).dispatch(any());
        assertFalse(service.attempt(1L));
        verify(tasks).finishFailed(1L, 1, "核券失败", now.plusSeconds(15), now);
        verify(tasks).appendLog(argThat(log -> log.notifyTimes() == 1 && "FAILED".equals(log.status())));
    }

    @Test
    void successfulOrExhaustedTasksAreNotDispatchedAgain() {
        when(tasks.find(1L)).thenReturn(task(1, 1, "SUCCESS"));
        assertTrue(service.attempt(1L));
        when(tasks.find(2L)).thenReturn(task(2, 8, "FAILED"));
        assertFalse(service.attempt(2L));
        verifyNoInteractions(dispatcher);
        verify(tasks, never()).claim(anyLong(), anyInt());
    }

    @Test
    void lostClaimReturnsCurrentSuccessState() {
        when(tasks.find(1L)).thenReturn(task(1, 0, "PENDING"));
        when(tasks.claim(1L, 0)).thenReturn(false);
        when(tasks.find(1L)).thenReturn(task(1, 1, "SUCCESS"));
        assertTrue(service.attempt(1L));
        verifyNoInteractions(dispatcher);
    }

    @Test
    void processDueAttemptsEachDueTask() {
        when(tasks.databaseNow()).thenReturn(now);
        when(tasks.findDue(now, 50)).thenReturn(List.of(task(1, 0, "PENDING"), task(2, 0, "PENDING")));
        when(tasks.find(1L)).thenReturn(task(1, 0, "PENDING"));
        when(tasks.find(2L)).thenReturn(task(2, 0, "PENDING"));
        when(tasks.claim(anyLong(), eq(0))).thenReturn(true);
        assertEquals(2, service.processDue(50));
        verify(dispatcher, times(2)).dispatch(any());
    }

    @Test
    void retryResetsFailedTaskAndRejectsSuccess() {
        when(tasks.find(1L)).thenReturn(task(1, 8, "FAILED"), task(1, 0, "PENDING"));
        when(tasks.databaseNow()).thenReturn(now);
        when(tasks.resetForRetry(1L, now)).thenReturn(true);
        when(tasks.claim(1L, 0)).thenReturn(true);
        assertTrue(service.retry(1L));
        when(tasks.find(2L)).thenReturn(task(2, 1, "SUCCESS"));
        assertThrows(IllegalStateException.class, () -> service.retry(2L));
        assertThrows(IllegalArgumentException.class, () -> service.retry(null));
    }
}
