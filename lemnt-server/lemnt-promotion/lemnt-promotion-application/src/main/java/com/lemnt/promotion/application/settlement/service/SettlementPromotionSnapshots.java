package com.lemnt.promotion.application.settlement.service;

import com.lemnt.promotion.domain.settlement.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** 在计算发生时记录来源并冻结分摊，后续退款不再次运行规则。 */
public final class SettlementPromotionSnapshots {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private SettlementPromotionSnapshots() {}

    /** 仅捕获本步骤实际减少的金额；积分为数量，不能再次折成现金优惠。 */
    public static SettlementPromotion capture(
            String key,
            String type,
            Long sourceId,
            String name,
            String rule,
            List<SettlementItem> items,
            Map<Long, BigDecimal> before,
            Map<Long, BigDecimal> after,
            long usePoint,
            long givePoint,
            Map<Long, Integer> coupons) {
        var children = new ArrayList<SettlementItemPromotion>();
        var discount = ZERO;
        long used = 0, given = 0;
        var basis = before.values().stream().reduce(ZERO, BigDecimal::add);
        var cumulative = ZERO;
        for (int index = 0; index < items.size(); index++) {
            var item = items.get(index);
            if (!before.containsKey(item.productId())) {
                continue;
            }
            var previous = before.get(item.productId());
            var delta = previous.subtract(after.get(item.productId()));
            if (delta.signum() < 0) {
                throw new IllegalStateException("优惠步骤不能增加商品金额");
            }
            discount = discount.add(delta);
            cumulative = cumulative.add(previous);
            long nextUse =
                    basis.signum() == 0
                            ? usePoint
                            : BigDecimal.valueOf(usePoint)
                                    .multiply(cumulative)
                                    .divide(basis, 0, RoundingMode.DOWN)
                                    .longValueExact();
            long nextGive =
                    basis.signum() == 0
                            ? givePoint
                            : BigDecimal.valueOf(givePoint)
                                    .multiply(cumulative)
                                    .divide(basis, 0, RoundingMode.DOWN)
                                    .longValueExact();
            children.add(
                    new SettlementItemPromotion(
                            null,
                            null,
                            null,
                            String.valueOf(index),
                            delta,
                            ZERO,
                            nextUse - used,
                            nextGive - given));
            used = nextUse;
            given = nextGive;
        }
        String snapshot =
                rule == null || rule.startsWith("{")
                        ? rule
                        : "{\"description\":\""
                                + rule.replace("\\", "\\\\").replace("\"", "\\\"")
                                + "\"}";
        return new SettlementPromotion(
                null, null, key, type, sourceId, name, name, snapshot, discount, ZERO, usePoint,
                givePoint, coupons, children);
    }

    /** 运费确定后完成原运费、包邮及支付赠积分的真实分摊；历史无来源的报价保留不完整。 */
    public static SettlementQuote finish(
            SettlementQuote quote,
            List<SettlementItem> cashItems,
            SettlementDelivery delivery,
            Map<Long, BigDecimal> originalFees,
            long ordinaryGift,
            int giftRate) {
        var paid = cashItems.stream().map(SettlementItem::payAmount).reduce(ZERO, BigDecimal::add);
        var totalGift = ordinaryGift + quote.rewardPoint();
        if (quote.deliveryOriginalPrice() == null) {
            return new SettlementQuote(
                    cashItems,
                    quote.originalAmount(),
                    quote.discountAmount(),
                    paid,
                    quote.couponId(),
                    quote.usePoint(),
                    totalGift,
                    delivery,
                    quote.rewardPoint(),
                    quote.freeDelivery(),
                    quote.giveCouponTemplateCounts());
        }
        var promotions = new ArrayList<SettlementPromotion>();
        for (var promotion : quote.promotions()) {
            if ("ORDER_GIFT".equals(promotion.promotionType())) {
                continue;
            }
            if (quote.freeDelivery() && "REWARD".equals(promotion.promotionType())) {
                var children = new ArrayList<SettlementItemPromotion>();
                for (int i = 0; i < cashItems.size(); i++) {
                    String key = String.valueOf(i);
                    var existing =
                            promotion.items().stream()
                                    .filter(v -> key.equals(v.quoteLineKey()))
                                    .findFirst();
                    var value =
                            existing.orElse(
                                    new SettlementItemPromotion(
                                            null, null, null, key, ZERO, ZERO, 0L, 0L));
                    children.add(
                            new SettlementItemPromotion(
                                    null,
                                    null,
                                    null,
                                    key,
                                    value.discountAmount(),
                                    originalFees.get(cashItems.get(i).productId()),
                                    value.usePoint(),
                                    value.givePoint()));
                }
                var fee = originalFees.values().stream().reduce(ZERO, BigDecimal::add);
                promotion =
                        new SettlementPromotion(
                                null,
                                null,
                                promotion.promotionKey(),
                                promotion.promotionType(),
                                promotion.sourceId(),
                                promotion.promotionName(),
                                promotion.ruleSummary(),
                                promotion.ruleSnapshot(),
                                promotion.discountAmount(),
                                fee,
                                promotion.usePoint(),
                                promotion.givePoint(),
                                promotion.giveCouponTemplateCounts(),
                                children);
            }
            promotions.add(promotion);
        }
        if (giftRate > 0) {
            var weights = new LinkedHashMap<Long, BigDecimal>();
            cashItems.forEach(item -> weights.put(item.productId(), item.payAmount()));
            promotions.add(
                    capture(
                            "ORDER_GIFT",
                            "ORDER_GIFT",
                            null,
                            "支付赠积分",
                            "{\"givePerYuan\":" + giftRate + "}",
                            cashItems,
                            weights,
                            weights,
                            0L,
                            ordinaryGift,
                            Map.of()));
        }
        var complete = new ArrayList<SettlementItem>();
        for (int i = 0; i < cashItems.size(); i++) {
            var item = cashItems.get(i);
            String key = String.valueOf(i);
            var allocations =
                    promotions.stream()
                            .flatMap(p -> p.items().stream())
                            .filter(a -> key.equals(a.quoteLineKey()))
                            .toList();
            var discount =
                    allocations.stream()
                            .map(SettlementItemPromotion::discountAmount)
                            .reduce(ZERO, BigDecimal::add);
            var deliveryDiscount =
                    allocations.stream()
                            .map(SettlementItemPromotion::deliveryDiscountPrice)
                            .reduce(ZERO, BigDecimal::add);
            long use = allocations.stream().mapToLong(SettlementItemPromotion::usePoint).sum();
            long give = allocations.stream().mapToLong(SettlementItemPromotion::givePoint).sum();
            complete.add(
                    new SettlementItem(
                            item.productId(),
                            item.productCode(),
                            item.productName(),
                            item.unitPrice(),
                            item.quantity(),
                            item.lineAmount(),
                            item.payAmount(),
                            item.deliveryPrice(),
                            item.spuId(),
                            item.spuName(),
                            item.specs(),
                            item.picUrl(),
                            item.orderItemId(),
                            item.commentStatus(),
                            discount,
                            originalFees.get(item.productId()),
                            deliveryDiscount,
                            ZERO,
                            use,
                            give,
                            0));
        }
        var originalFee = originalFees.values().stream().reduce(ZERO, BigDecimal::add);
        var discountFee =
                promotions.stream()
                        .map(SettlementPromotion::deliveryDiscountPrice)
                        .reduce(ZERO, BigDecimal::add);
        return new SettlementQuote(
                complete,
                quote.originalAmount(),
                quote.discountAmount(),
                paid,
                quote.couponId(),
                quote.usePoint(),
                totalGift,
                delivery,
                quote.rewardPoint(),
                quote.freeDelivery(),
                quote.giveCouponTemplateCounts(),
                promotions,
                originalFee,
                discountFee);
    }
}
