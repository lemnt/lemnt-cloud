package com.lemnt.trade.domain.fulfillment;

import com.lemnt.trade.domain.fulfillment.model.RefundAllocation;
import com.lemnt.trade.domain.order.model.OrderRefundItem;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RefundAllocationTest {
    @Test
    void allocationIsStableByPersistedItemIdAndPreservesEveryCent() {
        var a = new OrderRefundItem(1L, 10L, "商品一", 1, BigDecimal.TEN);
        var b = new OrderRefundItem(2L, 20L, "商品二", 1, BigDecimal.TEN);
        var c = new OrderRefundItem(3L, 30L, "商品三", 1, BigDecimal.TEN);
        var paid = new BigDecimal("19.99");
        var allocated = RefundAllocation.allocate(paid, List.of(c, a, b));
        assertEquals(new BigDecimal("6.66"), allocated.get(1L));
        assertEquals(new BigDecimal("6.66"), allocated.get(2L));
        assertEquals(new BigDecimal("6.67"), allocated.get(3L));
        assertEquals(paid, allocated.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        assertEquals(allocated, RefundAllocation.allocate(paid, List.of(a, b, c)));
    }

    @Test
    void inconsistentPaidAndItemSnapshotCannotProduceRefundQuota() {
        var item = new OrderRefundItem(1L, 10L, "商品", 1, BigDecimal.TEN);
        assertThrows(IllegalStateException.class,
                () -> RefundAllocation.allocate(new BigDecimal("10.01"), List.of(item)));
        assertThrows(IllegalStateException.class,
                () -> RefundAllocation.allocate(BigDecimal.TEN, List.of()));
    }
}
