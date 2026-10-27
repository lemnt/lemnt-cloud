package com.lemnt.mall.domain.promotion.service;

import com.lemnt.mall.domain.promotion.model.FenAmounts;
import com.lemnt.mall.domain.promotion.model.PricingException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Largest remainder allocation. Ties use numeric SKU ID, independent of map iteration order.
 */
public final class DiscountAllocator {
    private DiscountAllocator() {}

    /**
     * 分摊优惠金额。
     */
    public static Map<Long, Long> allocate(long discountFen, Map<Long, Long> weights) {
        PricingException.require(weights != null && weights.size() <= 100 && discountFen >= 0);
        long total = 0;
        for (var entry : weights.entrySet()) {
            PricingException.require(
                    entry.getKey() != null
                    && entry.getKey() > 0
                    && entry.getValue() != null
                    && entry.getValue() >= 0);
            total = FenAmounts.add(total, entry.getValue());
        }
        PricingException.require(discountFen <= total);
        var result = new TreeMap<Long, Long>();
        var remainders = new ArrayList<Remainder>();
        long allocated = 0;
        for (var entry : weights.entrySet()) {
            long part = 0;
            if (entry.getValue() > 0) {
                var parts =
                BigInteger.valueOf(discountFen)
                    .multiply(BigInteger.valueOf(entry.getValue()))
                    .divideAndRemainder(BigInteger.valueOf(total));
                part = parts[0].longValueExact();
                remainders.add(new Remainder(entry.getKey(), parts[1]));
            }
            result.put(entry.getKey(), part);
            allocated = FenAmounts.add(allocated, part);
        }
        remainders.sort(
                Comparator.comparing(Remainder::value)
                .reversed()
                .thenComparingLong(Remainder::skuId));
        long residual = discountFen - allocated;
        for (int i = 0; i < residual; i++) {
            long id = remainders.get(i).skuId();
            result.put(id, result.get(id) + 1);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * 余数。
     */
    private record Remainder(
            long skuId,
            BigInteger value
    ) {}
}
