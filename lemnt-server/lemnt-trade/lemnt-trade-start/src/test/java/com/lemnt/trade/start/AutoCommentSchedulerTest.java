package com.lemnt.trade.start;
import com.lemnt.trade.application.order.service.OrderCommentService;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
/** 调度使用数据库期限且逐单隔离异常。 */
class AutoCommentSchedulerTest {
    @Test
    void usesDatabaseDeadlineAndContinuesAfterOneFailure() {
        var orders=mock(TradeOrderGateway.class);
        var comments=mock(OrderCommentService.class);
        var now=LocalDateTime.of(2026,10,29,12,0);
        var cutoff=now.minusDays(7);
        when(orders.databaseNow()).thenReturn(now);
        when(orders.commentExpired(cutoff,50)).thenReturn(List.of("FIRST","SECOND"));
        when(comments.createSystem("FIRST",cutoff)).thenThrow(new IllegalStateException("模拟远程失败"));
        new TradeOrderAutoCommentScheduler(orders,comments,Duration.ofDays(7)).execute();
        verify(comments).createSystem("SECOND",cutoff);
        assertThrows(IllegalArgumentException.class,()->new TradeOrderAutoCommentScheduler(orders,comments,Duration.ZERO));
    }
}
