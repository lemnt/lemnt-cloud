package com.lemnt.mall.domain.promotion.service;

import com.lemnt.mall.domain.promotion.model.FenAmounts;
import com.lemnt.mall.domain.promotion.model.PricingContext;
import com.lemnt.mall.domain.promotion.model.PricingException;
import com.lemnt.mall.domain.promotion.model.PricingResult;
import com.lemnt.mall.domain.promotion.model.PromotionVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Pure promo-1 kernel. Does not reserve quota, persist settlements or invoke external systems.
 */
public final class PromotionPricingService {
    public static final String ALGORITHM_VERSION = "promo-1";

    /**
     * 价格。
     */
    public PricingResult price(PricingContext context) {
        PricingException.require(context != null);
        var gross = new TreeMap<Long, Long>();
        for (var line : context.lines()) {
            gross.put(line.productId(), line.grossFen());
        }
        long original = sum(gross);
        if (context.coupon() != null && context.coupon().rule().discount(original) == 0) {
            throw new PricingException(PricingException.Code.COUPON_NOT_ELIGIBLE);
        }

        // Baseline always preserves the explicit coupon; it is never silently removed.
        PricingResult best = candidate(context, gross, original, null);
        if (context.promotionMode() == PricingContext.Mode.NONE) {
            return best;
        }
        List<PromotionVersion> matched =
        context.campaigns().stream()
            .filter(c -> c.activeAt(context.nowEpochMs()))
            .filter(c -> context.lines().stream().anyMatch(c.scope()::matches))
            .toList();
        if (matched.size() > 100) {
            throw new PricingException(PricingException.Code.PROMOTION_CANDIDATE_LIMIT);
        }
        for (var campaign : matched) {
            if (context.coupon() != null
                    && campaign.couponPolicy() == PromotionVersion.CouponPolicy.EXCLUSIVE) {
                continue;
            }
            var result = candidate(context, gross, original, campaign);
            if (result != null && compare(result, best) < 0) {
                best = result;
            }
        }
        return best;
    }

    private PricingResult candidate(
            PricingContext context,
            Map<Long, Long> gross,
            long original,
            PromotionVersion campaign) {
        var weights = new TreeMap<Long, Long>();
        if (campaign != null) {
            for (var line : context.lines()) {
                if (campaign.scope().matches(line)) {
                    weights.put(line.productId(), gross.get(line.productId()));
                }
            }
        }
        long activityDiscount = campaign == null ? 0 : campaign.rule().discount(sum(weights));
        if (campaign != null && activityDiscount == 0) {
            return null;
        }
        var activityParts = DiscountAllocator.allocate(activityDiscount, weights);
        var remaining = new TreeMap<Long, Long>();
        for (var entry : gross.entrySet()) {
            remaining.put(
                    entry.getKey(),
                    entry.getValue() - activityParts.getOrDefault(entry.getKey(), 0L));
        }
        long couponDiscount =
        context.coupon() == null ? 0 : context.coupon().rule().discount(sum(remaining));
        if (context.coupon() != null && couponDiscount == 0) {
            return null;
        }
        var couponParts = DiscountAllocator.allocate(couponDiscount, remaining);
        var lines = new ArrayList<PricingResult.Line>();
        for (var line : context.lines()) {
            long id = line.productId();
            long payable = remaining.get(id) - couponParts.get(id);
            lines.add(
                    new PricingResult.Line(
                    id,
                    line.spuId(),
                    line.unitPriceFen(),
                    line.quantity(),
                    gross.get(id),
                    activityParts.getOrDefault(id, 0L),
                    couponParts.get(id),
                    payable,
                    payable / line.quantity(),
                    (int)(payable % line.quantity())));
        }
        return new PricingResult(
                ALGORITHM_VERSION,
                original,
                activityDiscount,
                couponDiscount,
                original - activityDiscount - couponDiscount,
                campaign,
                context.coupon() == null ? null : context.coupon().id(),
                lines);
    }

    private static long sum(Map<Long, Long> amounts) {
        long total = 0;
        for (long amount : amounts.values()) {
            total = FenAmounts.add(total, amount);
        }
        return total;
    }

    private static int compare(PricingResult left, PricingResult right) {
        int compared = Long.compare(left.payableFen(), right.payableFen());
        if (compared != 0) {
            return compared;
        }
        var a = left.selectedCampaign();
        var b = right.selectedCampaign();
        if (a == null || b == null) {
            return a == b ? 0 : a == null ? -1 : 1;
        }
        compared = Integer.compare(b.priority(), a.priority());
        if (compared != 0) {
            return compared;
        }
        compared = Long.compare(a.id(), b.id());
        return compared != 0 ? compared : Integer.compare(a.version(), b.version());
    }
}
