package com.lemnt.trade.domain.promotion.model;

import java.math.BigInteger;
import java.util.List;

/** Closed rule set: no scripts or expression evaluation. All amounts are fen. */
public sealed interface DiscountRule {
    long discount(long baseFen);

    record Tier(long thresholdFen, long discountFen) {
        public Tier {
            PricingException.require(thresholdFen >= 0 && discountFen > 0);
        }
    }

    record FullReduction(List<Tier> tiers) implements DiscountRule {
        public FullReduction {
            PricingException.require(tiers != null && !tiers.isEmpty() && tiers.size() <= 20);
            long threshold = -1;
            long discount = 0;
            for (var tier : tiers) {
                PricingException.require(
                        tier != null
                                && tier.thresholdFen() > threshold
                                && tier.discountFen() >= discount);
                threshold = tier.thresholdFen();
                discount = tier.discountFen();
            }
            tiers = List.copyOf(tiers);
        }

        @Override
        public long discount(long baseFen) {
            PricingException.require(baseFen >= 0);
            long result = 0;
            for (var tier : tiers) {
                if (baseFen < tier.thresholdFen()) break;
                result = tier.discountFen();
            }
            return Math.min(baseFen, result);
        }
    }

    record Percentage(long thresholdFen, int payRateBps, Long maxDiscountFen)
            implements DiscountRule {
        public Percentage {
            PricingException.require(thresholdFen >= 0 && payRateBps >= 1 && payRateBps <= 9999);
            PricingException.require(maxDiscountFen == null || maxDiscountFen > 0);
        }

        @Override
        public long discount(long baseFen) {
            PricingException.require(baseFen >= 0);
            if (baseFen < thresholdFen) return 0;
            long discount =
                    BigInteger.valueOf(baseFen)
                            .multiply(BigInteger.valueOf(10000L - payRateBps))
                            .divide(BigInteger.valueOf(10000))
                            .longValueExact();
            return maxDiscountFen == null ? discount : Math.min(discount, maxDiscountFen);
        }
    }

    record Fixed(long thresholdFen, long fixedAmountFen) implements DiscountRule {
        public Fixed {
            PricingException.require(thresholdFen >= 0 && fixedAmountFen > 0);
        }

        @Override
        public long discount(long baseFen) {
            PricingException.require(baseFen >= 0);
            return baseFen < thresholdFen ? 0 : Math.min(baseFen, fixedAmountFen);
        }
    }
}
