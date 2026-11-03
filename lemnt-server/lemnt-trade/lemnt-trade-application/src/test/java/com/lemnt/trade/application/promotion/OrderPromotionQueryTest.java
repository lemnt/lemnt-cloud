package com.lemnt.trade.application.promotion;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.promotion.executor.OrderPromotionDetailQueryExe;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.promotion.gateway.OrderPromotionGateway;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 查询历史未知与会员归属，禁止使用当前活动补历史。 */
class OrderPromotionQueryTest {
    final TradeOrderGateway orders = mock(TradeOrderGateway.class);
    final OrderPromotionGateway promotions = mock(OrderPromotionGateway.class);
    final OrderPromotionDetailQueryExe query = new OrderPromotionDetailQueryExe(orders, promotions);

    @Test
    void legacyUnknownStaysNull() {
        when(orders.findHeader("T1"))
                .thenReturn(new TradeOrder(1L, "T1", 7L, BigDecimal.TEN, "PAID", List.of(), null));
        when(promotions.find(1L)).thenReturn(List.of());
        var result = query.execute(7L, "T1");
        assertEquals("LEGACY_PARTIAL", result.snapshotStatus());
        assertNull(result.summary().discountAmount());
    }

    @Test
    void otherMemberCannotReadOrEnumeratePromotions() {
        when(orders.findHeader("T1"))
                .thenReturn(new TradeOrder(1L, "T1", 7L, BigDecimal.TEN, "PAID", List.of(), null));
        assertThrows(ServiceException.class, () -> query.execute(8L, "T1"));
        verifyNoInteractions(promotions);
    }

    @Test
    void missingMemberIdentityCannotFallBackToAdminRead() {
        assertThrows(ServiceException.class, () -> query.execute(null, "T1"));
        verifyNoInteractions(orders, promotions);
    }
}
