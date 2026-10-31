package com.lemnt.mall.domain.settlement;

import com.lemnt.mall.domain.coupon.model.Coupon;
import com.lemnt.mall.domain.coupon.model.CouponRules;
import com.lemnt.mall.domain.coupon.model.CouponTemplate;
import com.lemnt.mall.domain.settlement.model.SettlementItem;
import com.lemnt.mall.domain.settlement.model.SettlementRules;

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
        assertEquals(new BigDecimal("1.20"), SettlementRules.money(new BigDecimal("1.2"), false));
        for (String value : List.of("-1", "1.001", "100000000000000000.00")) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> SettlementRules.money(new BigDecimal(value), true));
        }
    }

    @Test
    void percentageMeansPayPercentWithCapAndDiscountRoundedDown() {
        var coupon = template("PERCENT", null, 80, new BigDecimal("5.00"));
        assertEquals(
                new BigDecimal("5.00"),
                CouponRules.discount(coupon, new BigDecimal("100.00"), now));
        assertEquals(
                new BigDecimal("2.19"),
                CouponRules.discount(coupon, new BigDecimal("10.99"), now));
    }

    @Test
    void validityIsLeftClosedRightOpenAndThresholdIsInclusive() {
        var coupon = template("FIXED", BigDecimal.ONE, null, null);
        assertEquals(
                new BigDecimal("1.00"),
                CouponRules.discount(coupon, BigDecimal.TEN, coupon.validFrom()));
        assertThrows(
                IllegalArgumentException.class,
                () -> CouponRules.discount(coupon, BigDecimal.TEN, coupon.validUntil()));
        assertThrows(
                IllegalArgumentException.class,
                () -> CouponRules.discount(coupon, new BigDecimal("9.99"), now));
    }

    @Test
    void quoteCapsDiscountAtGrossWithoutInventingAMinimumCharge() {
        var template = template("FIXED", new BigDecimal("20.00"), null, null);
        var line = new SettlementItem(1L, "S1", "sku", BigDecimal.TEN, 1, BigDecimal.TEN);
        var quote =
        SettlementRules.quote(
                List.of(line), new Coupon(2L, 1L, 3L, "AVAILABLE", null), template, now);
        assertEquals(new BigDecimal("0.00"), quote.payableAmount());
        assertThrows(
                IllegalArgumentException.class,
                () ->
                SettlementRules.quote(
                List.of(line),
                new Coupon(2L, 1L, 3L, "LOCKED", "O1"),
                template,
                now));
    }

    @Test
    void templatesRejectConflictingTypeParameters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CouponRules.template(template("FIXED", BigDecimal.ONE, 80, null)));
        assertThrows(
                IllegalArgumentException.class,
                () -> CouponRules.template(template("PERCENT", null, 101, null)));
    }
}
