package com.lemnt.order.application.promotion;

import com.lemnt.order.application.promotion.executor.AfterSalePromotionDetailQueryExe;
import com.lemnt.order.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.order.domain.aftersale.model.AfterSale;
import com.lemnt.order.domain.order.gateway.OrderBenefitsProgressGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.model.TradeOrder;
import com.lemnt.order.domain.promotion.gateway.OrderPromotionGateway;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 应执行权益与实际到账分开；历史冻结字段缺失只能报告未知。
 */
class AfterSalePromotionProgressQueryTest {
    final AfterSaleGateway afterSales = mock(AfterSaleGateway.class);
    final TradeOrderGateway orders = mock(TradeOrderGateway.class);
    final OrderPromotionGateway promotions = mock(OrderPromotionGateway.class);
    final OrderBenefitsProgressGateway progress = mock(OrderBenefitsProgressGateway.class);
    final AfterSalePromotionDetailQueryExe query =
        new AfterSalePromotionDetailQueryExe(afterSales, orders, promotions, progress);

    AfterSale seed(boolean complete) {
        var af =
            new AfterSale(
                "AF1",
                "O1",
                "REQ",
                7L,
                "P1",
                11L,
                1,
                "REFUND_ONLY",
                BigDecimal.ONE,
                "原因",
                "REFUNDED",
                null,
                null,
                null,
                LocalDateTime.now(),
                null,
                null,
                null,
                1L,
                7L,
                null,
                null,
                2L,
                3L,
                "SPU",
                null,
                null,
                complete ? BigDecimal.ZERO : null,
                complete ? BigDecimal.ZERO : null,
                complete ? 1L : null,
                complete ? 2L : null,
                complete ? 1 : null);
        when(afterSales.afterSale("AF1")).thenReturn(af);
        when(orders.findHeader("O1"))
            .thenReturn(new TradeOrder(7L, "O1", 7L, BigDecimal.TEN, "PAID", List.of(), null));
        when(promotions.find(7L)).thenReturn(List.of());
        when(promotions.refunds(List.of(1L))).thenReturn(List.of());
        return af;
    }

    @Test
    void publishedEventWithoutEffectsIsStillPending() {
        var af = seed(true);
        when(progress.refunded(af)).thenReturn(false);
        var v = query.execute("AF1");
        assertEquals("PENDING", v.benefitStatus());
        assertEquals(1L, v.returnUsePoint());
        assertEquals(2L, v.deductGivePoint());
    }

    @Test
    void onlyActualReceiptsShowSuccess() {
        var af = seed(true);
        when(progress.refunded(af)).thenReturn(true);
        assertEquals("SUCCESS", query.execute("AF1").benefitStatus());
    }

    @Test
    void legacyUnknownIsNotReportedAsWaitingOrSuccess() {
        seed(false);
        var v = query.execute("AF1");
        assertEquals("UNKNOWN", v.benefitStatus());
        assertEquals("LEGACY_PARTIAL", v.snapshotStatus());
        verifyNoInteractions(progress);
    }
}
