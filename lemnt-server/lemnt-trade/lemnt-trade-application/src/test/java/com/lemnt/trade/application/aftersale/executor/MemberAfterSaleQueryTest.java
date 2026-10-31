package com.lemnt.trade.application.aftersale.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.aftersale.qry.MemberAfterSalePageQry;
import com.lemnt.trade.application.aftersale.service.AfterSaleService;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.aftersale.model.AfterSale;
import com.lemnt.trade.domain.aftersale.model.AfterSaleFilter;
import com.lemnt.trade.domain.order.model.TradeOrder;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 会员售后查询与额度都以订单归属为前提。 */
class MemberAfterSaleQueryTest {
    final AfterSaleGateway afterSales = mock(AfterSaleGateway.class);
    final OrderPersistenceService persistence = mock(OrderPersistenceService.class);
    final AfterSaleService service = mock(AfterSaleService.class);
    final MemberAfterSalePageQueryExe pageQuery = new MemberAfterSalePageQueryExe(afterSales);
    final MemberAfterSaleDetailQueryExe detailQuery = new MemberAfterSaleDetailQueryExe(afterSales);
    final MemberAfterSaleItemListQueryExe itemQuery = new MemberAfterSaleItemListQueryExe(persistence, service);

    private AfterSale sale(Long memberId) {
        return new AfterSale("RF1", "TR1", "REQ1", memberId, "P1", 10L, 1, "REFUND_ONLY", BigDecimal.ONE, "r",
                "REQUESTED", null, null, null, LocalDateTime.now(), null, null, null, 1L, 1L, null, null);
    }

    private TradeOrder order(Long memberId) {
        return new TradeOrder(1L, "TR1", memberId, BigDecimal.TEN, "PAID", List.of(), LocalDateTime.now(), 0L, 0L);
    }

    @Test
    void pageFiltersByMemberInSql() {
        when(afterSales.afterSalePage(any())).thenReturn(PageResult.of(List.of(), 0L));
        pageQuery.execute(7L, new MemberAfterSalePageQry(null, "REQUESTED", 1, 10));
        ArgumentCaptor<AfterSaleFilter> captor = ArgumentCaptor.forClass(AfterSaleFilter.class);
        verify(afterSales).afterSalePage(captor.capture());
        assertEquals(7L, captor.getValue().memberId());
        assertEquals("REQUESTED", captor.getValue().status());
    }

    @Test
    void pageRejectsMissingMemberAndInvalidStatus() {
        assertThrows(IllegalArgumentException.class, () -> pageQuery.execute(null, new MemberAfterSalePageQry(null, null, 1, 10)));
        assertThrows(IllegalArgumentException.class, () -> pageQuery.execute(7L, new MemberAfterSalePageQry(null, "UNKNOWN", 1, 10)));
        verifyNoInteractions(afterSales);
    }

    @Test
    void detailOnlyReturnsOwnAfterSale() {
        when(afterSales.afterSale("RF1")).thenReturn(sale(7L));
        assertNotNull(detailQuery.execute(7L, "RF1"));
        assertNull(detailQuery.execute(8L, "RF1"));
        when(afterSales.afterSale("RF2")).thenReturn(null);
        assertNull(detailQuery.execute(7L, "RF2"));
    }

    @Test
    void itemsOnlyForOwnerOrder() {
        when(persistence.findByOrderNo("TR1")).thenReturn(order(7L));
        when(service.items("TR1")).thenReturn(List.of());
        assertNotNull(itemQuery.execute(7L, "TR1"));
        assertNull(itemQuery.execute(8L, "TR1"));
        verify(service, times(1)).items("TR1");
        when(persistence.findByOrderNo("TR2")).thenReturn(null);
        assertNull(itemQuery.execute(7L, "TR2"));
        verify(service, never()).items("TR2");
    }
}
