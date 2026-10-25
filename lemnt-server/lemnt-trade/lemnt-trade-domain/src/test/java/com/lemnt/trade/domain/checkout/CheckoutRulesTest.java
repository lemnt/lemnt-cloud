package com.lemnt.trade.domain.checkout;

import com.lemnt.trade.domain.checkout.model.*;
import com.lemnt.trade.domain.model.TradeOrderItem;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CheckoutRulesTest {
    LocalDateTime now = LocalDateTime.of(2026, 10, 25, 12, 0);

    CouponTemplate template(String kind, BigDecimal fixed, Integer percent, BigDecimal cap) {
        return new CouponTemplate(
                1L,
                "C1",
                "coupon",
                kind,
                new BigDecimal("10.00"),
                fixed,
                percent,
                cap,
                100,
                0,
                now.minusDays(1),
                now.plusDays(1),
                true);
    }

    @Test
    void exactMoneyRejectsRoundingAndOverflow() {
        assertEquals(new BigDecimal("1.20"), CheckoutRules.money(new BigDecimal("1.2"), false));
        for (String value : List.of("-1", "1.001", "100000000000000000.00"))
            assertThrows(
                    IllegalArgumentException.class,
                    () -> CheckoutRules.money(new BigDecimal(value), true));
    }

    @Test
    void percentageMeansPayPercentWithCapAndDiscountRoundedDown() {
        var coupon = template("PERCENT", null, 80, new BigDecimal("5.00"));
        assertEquals(
                new BigDecimal("5.00"),
                CheckoutRules.discount(coupon, new BigDecimal("100.00"), now));
        assertEquals(
                new BigDecimal("2.19"),
                CheckoutRules.discount(coupon, new BigDecimal("10.99"), now));
    }

    @Test
    void validityIsLeftClosedRightOpenAndThresholdIsInclusive() {
        var coupon = template("FIXED", BigDecimal.ONE, null, null);
        assertEquals(
                new BigDecimal("1.00"),
                CheckoutRules.discount(coupon, BigDecimal.TEN, coupon.validFrom()));
        assertThrows(
                IllegalArgumentException.class,
                () -> CheckoutRules.discount(coupon, BigDecimal.TEN, coupon.validUntil()));
        assertThrows(
                IllegalArgumentException.class,
                () -> CheckoutRules.discount(coupon, new BigDecimal("9.99"), now));
    }

    @Test
    void quoteCapsDiscountAtGrossWithoutInventingAMinimumCharge() {
        var template = template("FIXED", new BigDecimal("20.00"), null, null);
        var line = new TradeOrderItem(1L, "S1", "sku", BigDecimal.TEN, 1, BigDecimal.TEN);
        var quote =
                CheckoutRules.quote(
                        List.of(line), new Coupon(2L, 1L, 3L, "AVAILABLE", null), template, now);
        assertEquals(new BigDecimal("0.00"), quote.payableAmount());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        CheckoutRules.quote(
                                List.of(line),
                                new Coupon(2L, 1L, 3L, "LOCKED", "O1"),
                                template,
                                now));
    }

    @Test
    void templatesRejectConflictingTypeParameters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CheckoutRules.template(template("FIXED", BigDecimal.ONE, 80, null)));
        assertThrows(
                IllegalArgumentException.class,
                () -> CheckoutRules.template(template("PERCENT", null, 100, null)));
    }
}
