package com.lemnt.mall.domain.promotion.model;

import java.math.BigInteger;
import java.util.List;

/**
 * Closed rule set: no scripts or expression evaluation. All amounts are fen.
 */
public sealed interface DiscountRule {
    /**
     * 折扣。
     */
    long discount(long baseFen);

    /**
     * 阶梯。
     */
    record Tier(
            long thresholdFen,
            long discountFen
    ) {
        /**
         * 创建阶梯实例。
         */
        public Tier {
            PricingException.require(thresholdFen >= 0 && discountFen > 0);
        }
    }

    /**
     * 满减。
     */
    record FullReduction(
            List<Tier> tiers
    ) implements DiscountRule {
        /**
         * 创建满减实例。
         */
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
                if (baseFen < tier.thresholdFen()) {
                    break;
                }
                result = tier.discountFen();
            }
            return Math.min(baseFen, result);
        }
    }

    /**
     * 百分比。
     */
    record Percentage(
            long thresholdFen,
            int payRateBps,
            Long maxDiscountFen
    ) implements DiscountRule {
        /**
         * 创建百分比实例。
         */
        public Percentage {
            PricingException.require(thresholdFen >= 0 && payRateBps >= 1 && payRateBps <= 9999);
            PricingException.require(maxDiscountFen == null || maxDiscountFen > 0);
        }

        @Override
        public long discount(long baseFen) {
            PricingException.require(baseFen >= 0);
            if (baseFen < thresholdFen) {
                return 0;
            }
            long discount =
            BigInteger.valueOf(baseFen)
                .multiply(BigInteger.valueOf(10000L - payRateBps))
                .divide(BigInteger.valueOf(10000))
                .longValueExact();
            return maxDiscountFen == null ? discount : Math.min(discount, maxDiscountFen);
        }
    }

    /**
     * 固定。
     */
    record Fixed(
            long thresholdFen,
            long fixedAmountFen
    ) implements DiscountRule {
        /**
         * 创建固定实例。
         */
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
