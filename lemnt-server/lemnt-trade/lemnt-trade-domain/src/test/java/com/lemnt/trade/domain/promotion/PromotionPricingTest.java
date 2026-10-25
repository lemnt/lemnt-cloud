package com.lemnt.trade.domain.promotion;

import com.lemnt.trade.domain.checkout.model.CheckoutRules;
import com.lemnt.trade.domain.checkout.model.Coupon;
import com.lemnt.trade.domain.checkout.model.CouponTemplate;
import com.lemnt.trade.domain.promotion.model.*;
import com.lemnt.trade.domain.promotion.service.DiscountAllocator;
import com.lemnt.trade.domain.promotion.service.PromotionPricingService;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PromotionPricingTest {
    private final PromotionPricingService engine = new PromotionPricingService();
    private static final PromotionScope ALL =
            new PromotionScope(PromotionScope.Kind.ALL, List.of());
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 25, 12, 0);

    private static PromotionVersion campaign(
            long id, int version, int priority, DiscountRule rule) {
        return new PromotionVersion(
                id, version, priority, PromotionVersion.CouponPolicy.ALLOW_ONE, ALL, rule, 10, 20);
    }

    private static DiscountRule.FullReduction reduction(long discount) {
        return new DiscountRule.FullReduction(List.of(new DiscountRule.Tier(0, discount)));
    }

    private PricingResult price(
            List<PricingLine> lines, List<PromotionVersion> campaigns, SelectedCoupon coupon) {
        return engine.price(
                new PricingContext(lines, campaigns, coupon, PricingContext.Mode.BEST, 10));
    }

    @Test
    void allocationHandlesNumericTiesZeroWeightsAndLongIntermediate() {
        assertEquals(
                Map.of(2L, 1L, 10L, 0L, 20L, 0L),
                DiscountAllocator.allocate(1, Map.of(10L, 1L, 2L, 1L, 20L, 0L)));
        assertEquals(Map.of(1L, 0L), DiscountAllocator.allocate(0, Map.of(1L, 0L)));
        assertEquals(Map.of(), DiscountAllocator.allocate(0, Map.of()));
        long weight = Long.MAX_VALUE / 2;
        var allocated =
                DiscountAllocator.allocate(Long.MAX_VALUE - 2, Map.of(1L, weight, 2L, weight));
        assertEquals(weight, allocated.get(1L));
        assertEquals(weight - 1, allocated.get(2L));
    }

    @Test
    void twoThousandAllocationsMatchIndependentRationalOracle() {
        var random = new Random(20261025);
        for (int sample = 0; sample < 2000; sample++) {
            var weights = new LinkedHashMap<Long, Long>();
            int count = 1 + random.nextInt(100);
            for (long id = count; id > 0; id--)
                weights.put(id, sample % 11 == 0 ? 0L : random.nextLong(1_000_000_000_000L));
            long total = weights.values().stream().mapToLong(Long::longValue).sum();
            long discount = total == 0 ? 0 : random.nextLong(total + 1);
            var actual = DiscountAllocator.allocate(discount, weights);
            assertEquals(discount, actual.values().stream().mapToLong(Long::longValue).sum());
            for (var entry : weights.entrySet()) {
                long allocated = actual.get(entry.getKey());
                assertTrue(allocated >= 0 && allocated <= entry.getValue());
                if (total == 0) {
                    assertEquals(0L, allocated);
                    continue;
                }
                // Quota must be floor or ceil of the exact rational share.
                var numerator =
                        BigInteger.valueOf(discount).multiply(BigInteger.valueOf(entry.getValue()));
                var parts = numerator.divideAndRemainder(BigInteger.valueOf(total));
                long floor = parts[0].longValueExact();
                assertTrue(allocated == floor || (parts[1].signum() > 0 && allocated == floor + 1));
                // An awarded residual cent must outrank every unawarded residual cent.
                if (allocated > floor)
                    for (var other : weights.entrySet()) {
                        var otherParts =
                                BigInteger.valueOf(discount)
                                        .multiply(BigInteger.valueOf(other.getValue()))
                                        .divideAndRemainder(BigInteger.valueOf(total));
                        if (actual.get(other.getKey()) > otherParts[0].longValueExact()
                                || otherParts[1].signum() == 0) continue;
                        int rank = parts[1].compareTo(otherParts[1]);
                        assertTrue(rank > 0 || rank == 0 && entry.getKey() < other.getKey());
                    }
            }
            var reversed = new TreeMap<Long, Long>(Comparator.reverseOrder());
            reversed.putAll(weights);
            assertEquals(actual, DiscountAllocator.allocate(discount, reversed));
        }
    }

    @Test
    void randomizedFullPricingConservesMoneyAndStableUnitPartitionsUnderPermutation() {
        var random = new Random(6371);
        for (int sample = 0; sample < 500; sample++) {
            var lines = new ArrayList<PricingLine>();
            for (int i = 1; i <= 1 + sample % 12; i++)
                lines.add(
                        new PricingLine(
                                i,
                                i % 3 + 1,
                                random.nextLong(1, 1_000_000),
                                random.nextInt(1, 1000)));
            var campaigns = new ArrayList<PromotionVersion>();
            campaigns.add(campaign(2, 1, 10, reduction(random.nextLong(1, 10000))));
            campaigns.add(campaign(10, 2, 20, new DiscountRule.Percentage(0, 8000, null)));
            var coupon = new SelectedCoupon(5, new DiscountRule.Percentage(0, 9000, 5000L));
            var result = price(lines, campaigns, coupon);
            assertEquals(
                    result.originalFen(),
                    result.campaignDiscountFen()
                            + result.couponDiscountFen()
                            + result.payableFen());
            long campaignSum = 0, couponSum = 0, payableSum = 0;
            for (var line : result.lines()) {
                assertEquals(
                        line.grossFen(),
                        line.campaignDiscountFen() + line.couponDiscountFen() + line.payableFen());
                long unitSum = 0;
                for (int unit = 1; unit <= line.quantity(); unit++)
                    unitSum += line.unitPayableFen(unit);
                assertEquals(line.payableFen(), unitSum);
                assertTrue(
                        line.extraFenUnitCount() >= 0
                                && line.extraFenUnitCount() < line.quantity());
                campaignSum += line.campaignDiscountFen();
                couponSum += line.couponDiscountFen();
                payableSum += line.payableFen();
            }
            assertEquals(result.campaignDiscountFen(), campaignSum);
            assertEquals(result.couponDiscountFen(), couponSum);
            assertEquals(result.payableFen(), payableSum);
            Collections.shuffle(lines, random);
            Collections.shuffle(campaigns, random);
            assertEquals(result, price(lines, campaigns, coupon));
        }
    }

    @Test
    void legacyPercentageAndFixedDiscountsMatchActualJavaRules() {
        var random = new Random(871234);
        for (int sample = 0; sample < 1500; sample++) {
            long gross = random.nextLong(1, 1_000_000_000_000L);
            int percent = random.nextInt(1, 100);
            Long cap = sample % 3 == 0 ? null : random.nextLong(1, 1_000_000);
            var template = template("PERCENT", null, percent, cap);
            var coupon = SelectedCoupon.fromLegacy(7, ownedCoupon(), template, NOW);
            long expected =
                    FenAmounts.fromYuan(
                            CheckoutRules.discount(template, BigDecimal.valueOf(gross, 2), NOW));
            assertEquals(expected, coupon.rule().discount(gross));
            var fixed = template("FIXED", random.nextLong(1, 1_000_000), null, null);
            assertEquals(
                    FenAmounts.fromYuan(
                            CheckoutRules.discount(fixed, BigDecimal.valueOf(gross, 2), NOW)),
                    SelectedCoupon.fromLegacy(7, ownedCoupon(), fixed, NOW).rule().discount(gross));
        }
    }

    private static Coupon ownedCoupon() {
        return new Coupon(5L, 3L, 7L, "AVAILABLE", null);
    }

    private static CouponTemplate template(String kind, Long fixed, Integer percent, Long cap) {
        return new CouponTemplate(
                3L,
                "C",
                "coupon",
                kind,
                BigDecimal.ZERO,
                fixed == null ? null : BigDecimal.valueOf(fixed, 2),
                percent,
                cap == null ? null : BigDecimal.valueOf(cap, 2),
                100,
                1,
                NOW,
                NOW.plusHours(1),
                true);
    }

    @Test
    void legacyCouponOwnershipStatusTemplateAndWindowRejectWithoutDroppingSelection() {
        var template = template("PERCENT", null, 80, null);
        assertNotNull(SelectedCoupon.fromLegacy(7, ownedCoupon(), template, NOW));
        List<Coupon> invalid =
                Arrays.asList(
                        null,
                        new Coupon(5L, 3L, 8L, "AVAILABLE", null),
                        new Coupon(5L, 3L, 7L, "USED", null),
                        new Coupon(5L, 4L, 7L, "AVAILABLE", null),
                        new Coupon(5L, 3L, 7L, "AVAILABLE", "ORDER"),
                        new Coupon(null, 3L, 7L, "AVAILABLE", null));
        for (var coupon : invalid)
            assertCode(
                    PricingException.Code.COUPON_NOT_ELIGIBLE,
                    () -> SelectedCoupon.fromLegacy(7, coupon, template, NOW));
        for (var now : List.of(NOW.minusNanos(1), NOW.plusHours(1)))
            assertCode(
                    PricingException.Code.COUPON_NOT_ELIGIBLE,
                    () -> SelectedCoupon.fromLegacy(7, ownedCoupon(), template, now));
        assertCode(
                PricingException.Code.COUPON_NOT_ELIGIBLE,
                () -> SelectedCoupon.fromLegacy(7, ownedCoupon(), null, NOW));
    }

    @Test
    void noneRetainsCouponAndZeroBenefitCombinationCannotConsumeIt() {
        var lines = List.of(new PricingLine(1, 1, 100, 1));
        var campaigns = List.of(campaign(1, 1, 0, reduction(99)));
        var coupon = new SelectedCoupon(5, new DiscountRule.Percentage(0, 9900, null));
        var best = price(lines, campaigns, coupon);
        assertNull(best.selectedCampaign()); // after activity the coupon would benefit zero fen
        assertEquals(5L, best.couponId());
        assertEquals(99, best.payableFen());
        var none =
                engine.price(
                        new PricingContext(lines, campaigns, coupon, PricingContext.Mode.NONE, 10));
        assertEquals(best, none);
        assertCode(
                PricingException.Code.COUPON_NOT_ELIGIBLE,
                () -> price(List.of(new PricingLine(1, 1, 1, 1)), campaigns, coupon));
    }

    @Test
    void windowsAreLeftClosedRightOpenAndNonmatchingCampaignsDoNotCount() {
        var lines = List.of(new PricingLine(1, 1, 100, 1));
        var c = campaign(1, 1, 0, reduction(1));
        assertEquals(
                99,
                engine.price(
                                new PricingContext(
                                        lines, List.of(c), null, PricingContext.Mode.BEST, 10))
                        .payableFen());
        for (long now : List.of(9L, 20L))
            assertEquals(
                    100,
                    engine.price(
                                    new PricingContext(
                                            lines, List.of(c), null, PricingContext.Mode.BEST, now))
                            .payableFen());
        var nonmatching = new ArrayList<PromotionVersion>();
        for (int id = 1; id <= 101; id++)
            nonmatching.add(
                    new PromotionVersion(
                            id,
                            1,
                            0,
                            PromotionVersion.CouponPolicy.ALLOW_ONE,
                            new PromotionScope(PromotionScope.Kind.SKU_IDS, List.of(2L)),
                            reduction(1),
                            10,
                            20));
        assertEquals(100, price(lines, nonmatching, null).payableFen());
    }

    @Test
    void matchedCandidateLimitRejectsRatherThanTruncating() {
        var lines = List.of(new PricingLine(1, 1, 100, 1));
        var campaigns = new ArrayList<PromotionVersion>();
        for (int id = 1; id <= 100; id++) campaigns.add(campaign(id, 1, 0, reduction(1)));
        assertEquals(99, price(lines, campaigns, null).payableFen());
        campaigns.add(campaign(101, 1, 0, reduction(1)));
        assertCode(
                PricingException.Code.PROMOTION_CANDIDATE_LIMIT,
                () -> price(lines, campaigns, null));
        assertEquals(
                100,
                engine.price(
                                new PricingContext(
                                        lines, campaigns, null, PricingContext.Mode.NONE, 10))
                        .payableFen());
    }

    @Test
    void equalVersionsUseSmallerVersionAndExtremePrioritiesCannotOverflow() {
        var lines = List.of(new PricingLine(1, 1, 100, 1));
        assertEquals(
                1,
                price(
                                lines,
                                List.of(
                                        campaign(2, 2, 0, reduction(1)),
                                        campaign(2, 1, 0, reduction(1))),
                                null)
                        .selectedCampaign()
                        .version());
        assertEquals(
                10,
                price(
                                lines,
                                List.of(
                                        campaign(2, 1, Integer.MIN_VALUE, reduction(1)),
                                        campaign(10, 1, Integer.MAX_VALUE, reduction(1))),
                                null)
                        .selectedCampaign()
                        .id());
    }

    @Test
    void zeroActivityDoesNotHoldResourceAndZeroPayRemainsVisible() {
        var result =
                price(
                        List.of(new PricingLine(1, 1, 1, 1)),
                        List.of(campaign(1, 1, 1, new DiscountRule.Percentage(0, 9999, null))),
                        null);
        assertNull(result.selectedCampaign());
        var zero =
                price(
                        List.of(new PricingLine(1, 1, 1, 1)),
                        List.of(campaign(1, 1, 1, reduction(2))),
                        null);
        assertEquals(0, zero.payableFen());
        assertFalse(zero.submittable());
        assertEquals("ZERO_PAY_UNSUPPORTED", zero.blockedReason());
        assertEquals(0, zero.lines().get(0).unitPayableFen(1));
    }

    @Test
    void yuanConversionAndOrderSumRejectPrecisionNegativesAndOverflow() {
        assertEquals(120, FenAmounts.fromYuan(new BigDecimal("1.2000")));
        assertEquals(Long.MAX_VALUE, FenAmounts.fromYuan(new BigDecimal("92233720368547758.07")));
        for (var bad :
                Arrays.asList(
                        null,
                        new BigDecimal("-0.01"),
                        new BigDecimal("0.001"),
                        new BigDecimal("92233720368547758.08")))
            assertCode(PricingException.Code.AMOUNT_OUT_OF_RANGE, () -> FenAmounts.fromYuan(bad));
        assertCode(
                PricingException.Code.AMOUNT_OUT_OF_RANGE,
                () ->
                        price(
                                List.of(
                                        new PricingLine(1, 1, Long.MAX_VALUE, 1),
                                        new PricingLine(2, 1, 1, 1)),
                                List.of(),
                                null));
        assertCode(
                PricingException.Code.AMOUNT_OUT_OF_RANGE,
                () -> price(List.of(new PricingLine(1, 1, Long.MAX_VALUE, 2)), List.of(), null));
    }

    @Test
    void malformedRulesScopesLinesAndDuplicateVersionsReject() {
        List<Runnable> bad =
                List.of(
                        () -> new DiscountRule.FullReduction(List.of()),
                        () ->
                                new DiscountRule.FullReduction(
                                        Collections.nCopies(21, new DiscountRule.Tier(0, 1))),
                        () ->
                                new DiscountRule.FullReduction(
                                        List.of(
                                                new DiscountRule.Tier(2, 1),
                                                new DiscountRule.Tier(1, 2))),
                        () ->
                                new DiscountRule.FullReduction(
                                        List.of(
                                                new DiscountRule.Tier(1, 2),
                                                new DiscountRule.Tier(2, 1))),
                        () -> new DiscountRule.Percentage(0, 10000, null),
                        () -> new DiscountRule.Percentage(0, 0, null),
                        () -> new DiscountRule.Percentage(0, 8000, 0L),
                        () -> new DiscountRule.Fixed(-1, 2),
                        () -> new SelectedCoupon(1, new DiscountRule.Percentage(0, 7501, null)),
                        () -> new SelectedCoupon(1, reduction(1)),
                        () -> campaign(1, 1, 0, new DiscountRule.Fixed(0, 1)),
                        () -> new PromotionScope(PromotionScope.Kind.ALL, List.of(1L)),
                        () -> new PromotionScope(PromotionScope.Kind.SPU_IDS, List.of()),
                        () -> new PromotionScope(PromotionScope.Kind.SKU_IDS, List.of(1L, 1L)),
                        () -> new PromotionScope(PromotionScope.Kind.SKU_IDS, List.of(-1L)),
                        () -> new PricingLine(1, 1, 1, 1000),
                        () -> new PricingLine(0, 1, 1, 1),
                        () ->
                                new PricingContext(
                                        List.of(), List.of(), null, PricingContext.Mode.BEST, 10),
                        () ->
                                new PricingContext(
                                        Collections.nCopies(101, new PricingLine(1, 1, 1, 1)),
                                        List.of(),
                                        null,
                                        PricingContext.Mode.BEST,
                                        10),
                        () ->
                                price(
                                        List.of(
                                                new PricingLine(1, 1, 1, 1),
                                                new PricingLine(1, 2, 1, 1)),
                                        List.of(),
                                        null),
                        () ->
                                price(
                                        List.of(new PricingLine(1, 1, 1, 1)),
                                        List.of(
                                                campaign(1, 1, 0, reduction(1)),
                                                campaign(1, 1, 1, reduction(2))),
                                        null));
        for (var operation : bad)
            assertCode(PricingException.Code.INVALID_PROMOTION_REQUEST, operation);
        assertCode(
                PricingException.Code.INVALID_PROMOTION_REQUEST,
                () -> DiscountAllocator.allocate(2, Map.of(1L, 1L)));
    }

    @Test
    void inputSnapshotsAreDefensiveAndOutputCollectionsCannotBeMutated() {
        var ids = new ArrayList<>(List.of(10L, 2L));
        var scope = new PromotionScope(PromotionScope.Kind.SKU_IDS, ids);
        ids.clear();
        assertEquals(List.of(2L, 10L), scope.ids());
        var lines = new ArrayList<>(List.of(new PricingLine(1, 1, 100, 1)));
        var campaigns = new ArrayList<>(List.of(campaign(1, 1, 0, reduction(1))));
        var context = new PricingContext(lines, campaigns, null, PricingContext.Mode.BEST, 10);
        lines.clear();
        campaigns.clear();
        var result = engine.price(context);
        assertEquals(99, result.payableFen());
        assertThrows(UnsupportedOperationException.class, () -> result.lines().clear());
        assertThrows(UnsupportedOperationException.class, () -> context.campaigns().clear());
        assertThrows(UnsupportedOperationException.class, () -> scope.ids().clear());
        assertCode(
                PricingException.Code.INVALID_PROMOTION_REQUEST,
                () -> result.lines().get(0).unitPayableFen(0));
        assertCode(
                PricingException.Code.INVALID_PROMOTION_REQUEST,
                () -> result.lines().get(0).unitPayableFen(2));
    }

    @Test
    void sameSpuSkusShareScopedDiscountWithoutTouchingOtherSpus() {
        var scope = new PromotionScope(PromotionScope.Kind.SPU_IDS, List.of(7L));
        var campaign =
                new PromotionVersion(
                        1,
                        1,
                        0,
                        PromotionVersion.CouponPolicy.ALLOW_ONE,
                        scope,
                        reduction(3),
                        10,
                        20);
        var result =
                price(
                        List.of(
                                new PricingLine(2, 7, 1, 1),
                                new PricingLine(10, 7, 1, 1),
                                new PricingLine(3, 8, 100, 1)),
                        List.of(campaign),
                        new SelectedCoupon(5, new DiscountRule.Fixed(0, 1)));
        assertEquals(2, result.campaignDiscountFen());
        assertEquals(1, result.couponDiscountFen());
        assertEquals(99, result.payableFen());
        assertEquals(
                List.of(2L, 3L, 10L),
                result.lines().stream().map(PricingResult.Line::productId).toList());
        var unrelated = result.lines().get(1);
        assertEquals(0, unrelated.campaignDiscountFen());
        assertEquals(1, unrelated.couponDiscountFen());
        assertEquals(0, result.lines().get(0).couponDiscountFen());
        assertEquals(0, result.lines().get(2).couponDiscountFen());
    }

    @Test
    void scopeAndCartBoundaryLimitsAreInclusiveAndRejectNulls() {
        var ids = new ArrayList<Long>();
        for (long id = 1; id <= 1000; id++) ids.add(id);
        assertEquals(1000, new PromotionScope(PromotionScope.Kind.SKU_IDS, ids).ids().size());
        ids.add(1001L);
        assertCode(
                PricingException.Code.INVALID_PROMOTION_REQUEST,
                () -> new PromotionScope(PromotionScope.Kind.SKU_IDS, ids));
        assertCode(
                PricingException.Code.INVALID_PROMOTION_REQUEST,
                () -> new PromotionScope(PromotionScope.Kind.SKU_IDS, Arrays.asList((Long) null)));
        assertCode(
                PricingException.Code.INVALID_PROMOTION_REQUEST,
                () ->
                        new PricingContext(
                                Arrays.asList((PricingLine) null),
                                List.of(),
                                null,
                                PricingContext.Mode.BEST,
                                10));
        assertCode(
                PricingException.Code.INVALID_PROMOTION_REQUEST,
                () -> new DiscountRule.FullReduction(Arrays.asList((DiscountRule.Tier) null)));
        var lines = new ArrayList<PricingLine>();
        for (long id = 1; id <= 100; id++) lines.add(new PricingLine(id, 1, 1, 999));
        assertEquals(99900, price(lines, List.of(), null).originalFen());
    }

    @Test
    void disabledAndMalformedLegacyTemplatesFailClosed() {
        var disabled =
                new CouponTemplate(
                        3L,
                        "C",
                        "coupon",
                        "FIXED",
                        BigDecimal.ZERO,
                        BigDecimal.ONE,
                        null,
                        null,
                        100,
                        1,
                        NOW,
                        NOW.plusHours(1),
                        false);
        assertCode(
                PricingException.Code.COUPON_NOT_ELIGIBLE,
                () -> SelectedCoupon.fromLegacy(7, ownedCoupon(), disabled, NOW));
        var unknown = template("UNKNOWN", 1L, null, null);
        assertCode(
                PricingException.Code.INVALID_PROMOTION_REQUEST,
                () -> SelectedCoupon.fromLegacy(7, ownedCoupon(), unknown, NOW));
        var percentWithFixed = template("PERCENT", 1L, 80, null);
        assertCode(
                PricingException.Code.INVALID_PROMOTION_REQUEST,
                () -> SelectedCoupon.fromLegacy(7, ownedCoupon(), percentWithFixed, NOW));
        var imprecise =
                new CouponTemplate(
                        3L,
                        "C",
                        "coupon",
                        "FIXED",
                        new BigDecimal("0.001"),
                        BigDecimal.ONE,
                        null,
                        null,
                        100,
                        1,
                        NOW,
                        NOW.plusHours(1),
                        true);
        assertCode(
                PricingException.Code.AMOUNT_OUT_OF_RANGE,
                () -> SelectedCoupon.fromLegacy(7, ownedCoupon(), imprecise, NOW));
    }

    private static void assertCode(PricingException.Code expected, Runnable operation) {
        assertEquals(expected, assertThrows(PricingException.class, operation::run).code());
    }
}
