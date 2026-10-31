package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.dto.OrderDetailDTO;
import com.lemnt.trade.application.fulfillment.qry.MemberOrderPageQry;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.fulfillment.model.FulfillmentFilter;
import com.lemnt.trade.domain.fulfillment.model.OrderPage;
import com.lemnt.trade.domain.fulfillment.model.OrderSummary;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.OrderStatusCount;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.order.model.TradeOrderItem;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 会员订单查询在 SQL 过滤与归属核验上都不信任客户端身份。 */
class MemberOrderQueryTest {
    final TradeOrderGateway orders = mock(TradeOrderGateway.class);
    final OrderPersistenceService persistence = mock(OrderPersistenceService.class);
    final MemberOrderPageQueryExe pageQuery = new MemberOrderPageQueryExe(orders, persistence);
    final OrderDetailQueryExe detail = mock(OrderDetailQueryExe.class);
    final MemberOrderDetailQueryExe detailQuery = new MemberOrderDetailQueryExe(detail, persistence);
    final MemberOrderStatusCountQueryExe countQuery = new MemberOrderStatusCountQueryExe(orders);

    @Test
    void pageFiltersByMemberAndIncludesOrderItems() {
        var summary = new OrderSummary("TR1", 7L, BigDecimal.TEN, "PAID", LocalDateTime.now(), true, false,
                null, null, null, null);
        when(orders.orders(any())).thenReturn(new OrderPage(List.of(summary), 1L));
        when(persistence.findByOrderNo("TR1")).thenReturn(new TradeOrder(1L, "TR1", 7L, BigDecimal.TEN, "PAID",
                List.of(new TradeOrderItem(5L, "P5", "商品", BigDecimal.TEN, 1, BigDecimal.TEN)), LocalDateTime.now(),
                0L, 0L));
        var result = pageQuery.execute(7L, new MemberOrderPageQry("PAID", null, 2, 10));
        assertEquals(1, result.getRecords().size());
        assertEquals(1, result.getRecords().get(0).items().size());
        ArgumentCaptor<FulfillmentFilter> captor = ArgumentCaptor.forClass(FulfillmentFilter.class);
        verify(orders).orders(captor.capture());
        assertEquals(7L, captor.getValue().memberId());
        assertEquals("PAID", captor.getValue().status());
        assertEquals(10, captor.getValue().limit());
        assertEquals(10, captor.getValue().offset());
    }

    @Test
    void pageRejectsInvalidPagingStatusAndMissingMember() {
        assertThrows(IllegalArgumentException.class, () -> pageQuery.execute(null, new MemberOrderPageQry(null, null, 1, 10)));
        assertThrows(IllegalArgumentException.class, () -> pageQuery.execute(7L, new MemberOrderPageQry(null, null, 0, 10)));
        assertThrows(IllegalArgumentException.class, () -> pageQuery.execute(7L, new MemberOrderPageQry("UNKNOWN", null, 1, 10)));
        verifyNoInteractions(orders);
    }

    @Test
    void detailOnlyReturnsOwnOrder() {
        when(persistence.findByOrderNo("TR1")).thenReturn(new TradeOrder(1L, "TR1", 7L, BigDecimal.TEN, "PAID",
                List.of(), LocalDateTime.now(), 0L, 0L));
        var value = new OrderDetailDTO("TR1", 7L, BigDecimal.TEN, "PAID", true, false, List.of(), null, false, null);
        when(detail.execute("TR1")).thenReturn(value);
        assertNotNull(detailQuery.execute(7L, "TR1"));
        assertNull(detailQuery.execute(8L, "TR1"));
        when(persistence.findByOrderNo("TR2")).thenReturn(null);
        assertNull(detailQuery.execute(7L, "TR2"));
    }

    @Test
    void statusCountsMapCompletedByCommentState() {
        when(orders.memberStatusCounts(7L)).thenReturn(List.of(
                new OrderStatusCount("RESERVING", false, 1),
                new OrderStatusCount("PENDING_PAYMENT", false, 1),
                new OrderStatusCount("PAID", false, 2),
                new OrderStatusCount("SHIPPED", false, 3),
                new OrderStatusCount("COMPLETED", false, 4),
                new OrderStatusCount("COMPLETED", true, 5),
                new OrderStatusCount("CLOSED", false, 6),
                new OrderStatusCount("REFUNDED", false, 7)));
        var value = countQuery.execute(7L);
        assertEquals(2, value.pendingPayment());
        assertEquals(2, value.awaitingShipment());
        assertEquals(3, value.awaitingReceipt());
        assertEquals(4, value.awaitingComment());
        assertEquals(5, value.completed());
        assertEquals(6, value.closed());
        assertEquals(7, value.refunded());
    }
}
