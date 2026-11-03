package com.lemnt.order.domain.promotion.service;

import com.lemnt.order.domain.order.model.TradeOrder;
import com.lemnt.order.domain.promotion.model.OrderItemPromotion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** 校验可信报价的金额及权益守恒，不重新运行商城促销规则。 */
public final class OrderPricingRules {
    private OrderPricingRules() {}

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    public static void validate(TradeOrder order) {
        if (!"COMPLETE".equals(order.pricingSnapshotStatus())) {
            return;
        }
        if (order.pricingSnapshotVersion() == null || order.pricingSnapshotVersion() < 1) {
            fail();
        }
        var original = ZERO;
        var discount = ZERO;
        var freight = ZERO;
        var freightDiscount = ZERO;
        var adjustment = ZERO;
        var pay = ZERO;
        long use = 0, give = 0;
        var keys = new HashSet<String>();
        var couponCounts = new HashMap<Long, Integer>();
        var children = new HashMap<String, List<OrderItemPromotion>>();
        var pd = ZERO;
        var pf = ZERO;
        long pu = 0, pg = 0;
        for (var promotion : order.promotions()) {
            if (promotion.promotionKey() == null
                    || promotion.promotionKey().isBlank()
                    || promotion.promotionKey().length() > 96
                    || !keys.add(promotion.promotionKey())
                    || promotion.promotionName() == null
                    || promotion.promotionName().isBlank()
                    || !Set.of(
                                    "SECKILL",
                                    "COMBINATION",
                                    "BARGAIN",
                                    "POINT_EXCHANGE",
                                    "DISCOUNT",
                                    "REWARD",
                                    "COUPON",
                                    "POINT_DEDUCT",
                                    "MEMBER_LEVEL",
                                    "ORDER_GIFT")
                            .contains(promotion.promotionType())) {
                fail();
            }
            var d = ZERO;
            var f = ZERO;
            long u = 0, g = 0;
            var itemKeys = new HashSet<String>();
            for (var item : promotion.items()) {
                int index;
                try {
                    index = Integer.parseInt(item.quoteLineKey());
                } catch (RuntimeException error) {
                    throw new IllegalArgumentException("优惠行标识不合法");
                }
                if (index < 0
                        || index >= order.items().size()
                        || !itemKeys.add(item.quoteLineKey())) {
                    fail();
                }
                d = d.add(money(item.discountAmount()));
                f = f.add(money(item.deliveryDiscountPrice()));
                u = Math.addExact(u, points(item.usePoint()));
                g = Math.addExact(g, points(item.givePoint()));
                children.computeIfAbsent(item.quoteLineKey(), ignored -> new ArrayList<>())
                        .add(item);
            }
            equal(d, promotion.discountAmount());
            equal(f, promotion.deliveryDiscountPrice());
            if (u != points(promotion.usePoint()) || g != points(promotion.givePoint())) {
                fail();
            }
            pd = pd.add(d);
            pf = pf.add(f);
            pu = Math.addExact(pu, u);
            pg = Math.addExact(pg, g);
            if (promotion.giveCouponTemplateCounts() == null) {
                fail();
            }
            promotion
                    .giveCouponTemplateCounts()
                    .forEach(
                            (id, count) -> {
                                if (id == null || id <= 0 || count == null || count <= 0) {
                                    fail();
                                }
                                couponCounts.merge(id, count, Math::addExact);
                            });
        }
        for (int index = 0; index < order.items().size(); index++) {
            var item = order.items().get(index);
            var d = money(item.discountAmount());
            var fo = money(item.deliveryOriginalPrice());
            var fd = money(item.deliveryDiscountPrice());
            var a = signed(item.adjustmentAmount());
            var p = money(item.payAmount());
            if (d.compareTo(item.lineAmount()) > 0
                    || fd.compareTo(fo) > 0
                    || item.refundedQuantity() == null
                    || item.refundedQuantity() != 0) {
                fail();
            }
            equal(p, item.lineAmount().subtract(d).add(fo).subtract(fd).add(a));
            equal(item.deliveryPrice(), fo.subtract(fd));
            var rows = children.getOrDefault(String.valueOf(index), List.of());
            equal(
                    d,
                    rows.stream()
                            .map(OrderItemPromotion::discountAmount)
                            .reduce(ZERO, BigDecimal::add));
            equal(
                    fd,
                    rows.stream()
                            .map(OrderItemPromotion::deliveryDiscountPrice)
                            .reduce(ZERO, BigDecimal::add));
            long u = points(item.usePoint()), g = points(item.givePoint());
            if (u != rows.stream().mapToLong(OrderItemPromotion::usePoint).sum()
                    || g != rows.stream().mapToLong(OrderItemPromotion::givePoint).sum()) {
                fail();
            }
            original = original.add(money(item.lineAmount()));
            discount = discount.add(d);
            freight = freight.add(fo);
            freightDiscount = freightDiscount.add(fd);
            adjustment = adjustment.add(a);
            pay = pay.add(p);
            use = Math.addExact(use, u);
            give = Math.addExact(give, g);
        }
        equal(original, order.originalAmount());
        equal(discount, order.discountAmount());
        equal(freight, order.deliveryOriginalPrice());
        equal(freightDiscount, order.deliveryDiscountPrice());
        equal(adjustment, order.adjustmentAmount());
        equal(pay, order.totalAmount());
        equal(pd, discount);
        equal(pf, freightDiscount);
        if (pu != use
                || pg != give
                || use != order.usePoint()
                || give != order.givePoint()
                || !couponCounts.equals(order.giveCouponTemplateCounts())) {
            fail();
        }
    }

    private static BigDecimal signed(BigDecimal value) {
        if (value == null) {
            fail();
        }
        return value.setScale(2, RoundingMode.UNNECESSARY);
    }

    private static BigDecimal money(BigDecimal value) {
        var amount = signed(value);
        if (amount.signum() < 0) {
            fail();
        }
        return amount;
    }

    private static long points(Long value) {
        if (value == null || value < 0) {
            fail();
        }
        return value;
    }

    private static void equal(BigDecimal a, BigDecimal b) {
        if (a == null || b == null || a.compareTo(b) != 0) {
            fail();
        }
    }

    private static void fail() {
        throw new IllegalArgumentException("订单金额、优惠或权益快照不完整或不守恒");
    }
}
