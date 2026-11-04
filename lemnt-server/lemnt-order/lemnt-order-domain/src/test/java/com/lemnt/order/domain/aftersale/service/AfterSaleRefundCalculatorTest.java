package com.lemnt.order.domain.aftersale.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 区分固定单件基准与剩余额度均摊，校验末次守恒和非法历史额度。
 */
class AfterSaleRefundCalculatorTest {
    private BigDecimal cash(String original, int q, int r, String refunded, int requested) {
        return AfterSaleRefundCalculator.cash(
            new BigDecimal(original), q, r, new BigDecimal(refunded), requested);
    }

    @Test
    void fixedUnitAndFinalRemainder() {
        assertEquals(new BigDecimal("3.32"), cash("10.00", 6, 0, "0.00", 2));
        assertEquals(new BigDecimal("4.98"), cash("10.00", 6, 2, "3.32", 3));
        assertEquals(new BigDecimal("1.70"), cash("10.00", 6, 5, "8.30", 1));
    }

    @Test
    void finalMultipleUnitsAndDirectFullRefund() {
        assertEquals(new BigDecimal("3.36"), cash("10.00", 6, 4, "6.64", 2));
        assertEquals(new BigDecimal("10.00"), cash("10.00", 6, 0, "0", 6));
    }

    @Test
    void zeroCashSharesRemainRefundableByQuantity() {
        assertEquals(new BigDecimal("0.00"), cash("0.01", 3, 0, "0.00", 1));
        assertEquals(new BigDecimal("0.00"), cash("0.01", 3, 1, "0.00", 1));
        assertEquals(new BigDecimal("0.01"), cash("0.01", 3, 2, "0.00", 1));
        assertEquals(new BigDecimal("0.00"), cash("0.00", 3, 1, "0.00", 2));
    }

    @Test
    void pointsRetainCumulativeRuleAndAvoidLongOverflow() {
        assertEquals(1, AfterSaleRefundCalculator.points(5, 3, 0, 1));
        assertEquals(2, AfterSaleRefundCalculator.points(5, 3, 1, 1));
        assertEquals(2, AfterSaleRefundCalculator.points(5, 3, 2, 1));
        assertEquals(Long.MAX_VALUE, AfterSaleRefundCalculator.points(Long.MAX_VALUE, 999, 0, 999));
    }

    @Test
    void rejectOverRefundAndInconsistentHistory() {
        assertThrows(IllegalArgumentException.class, () -> cash("10.00", 6, 5, "8.30", 2));
        assertThrows(IllegalArgumentException.class, () -> cash("10.00", 6, 2, "10.01", 1));
        assertThrows(IllegalStateException.class, () -> cash("10.00", 6, 2, "9.00", 1));
    }

    @Test
    void everySequentialQuantityPartitionClosesExactly() {
        for (int fen = 0; fen <= 250; fen++)
            for (int quantity = 1; quantity <= 12; quantity++) {
                var total = BigDecimal.valueOf(fen, 2);
                var refunded = BigDecimal.ZERO;
                for (int previous = 0; previous < quantity; previous++) {
                    var current =
                        AfterSaleRefundCalculator.cash(total, quantity, previous, refunded, 1);
                    assertTrue(current.signum() >= 0);
                    refunded = refunded.add(current);
                }
                assertEquals(0, total.compareTo(refunded));
            }
    }
}
