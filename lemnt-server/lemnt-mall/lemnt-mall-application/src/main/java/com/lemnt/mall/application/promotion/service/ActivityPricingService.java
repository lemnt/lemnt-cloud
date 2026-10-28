package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.domain.coupon.model.Coupon;
import com.lemnt.mall.domain.coupon.model.CouponRules;
import com.lemnt.mall.domain.member.gateway.MemberTradeGateway;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.promotion.gateway.DiscountActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.DiscountProductGateway;
import com.lemnt.mall.domain.promotion.gateway.RewardActivityGateway;
import com.lemnt.mall.domain.promotion.model.RewardActivity;
import com.lemnt.mall.domain.settlement.model.SettlementItem;
import com.lemnt.mall.domain.settlement.model.SettlementQuote;
import com.lemnt.mall.domain.settlement.model.SettlementRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 按 SKU 折扣、满减、优惠券顺序计算实付，商品原价与行实付分别保留。 */
@Component
@RequiredArgsConstructor
public class ActivityPricingService {
    private final MemberTradeGateway memberTradeGateway;
    private final DiscountActivityGateway discounts;
    private final DiscountProductGateway products;
    private final RewardActivityGateway rewards;
    private final ProductSkuGateway skus;
    private final ProductSpuGateway spus;

    /** 使用服务器当前规则重新计算，客户端不能提交价格或优惠结果。 */
    public SettlementQuote price(List<SettlementItem> items, Coupon coupon,
            LocalDateTime now, int memberDiscountPercent) {
        return price(items, coupon, now, memberDiscountPercent, false, 0L);
    }

    /** 积分抵扣与赠送以本次服务器配置和余额计算，并写入结算快照。 */
    public SettlementQuote price(List<SettlementItem> items, Coupon coupon,
            LocalDateTime now, int memberDiscountPercent, boolean pointStatus, long points) {
        if (memberDiscountPercent < 0 || memberDiscountPercent > 100 || points < 0) {
            throw new ServiceException("会员折扣或积分余额不合法", 400);
        }
        // 先复用基础报价校验商品金额和券归属，券门槛在促销后再次计算。
        var original = SettlementRules.quote(items, null, null, now).originalAmount();
        var active = discounts.enabled().stream().filter(a -> !now.isBefore(a.startTime())
                && now.isBefore(a.endTime())).map(a -> a.id()).toList();
        var configurations = products.byActivities(active);
        Map<Long, BigDecimal> remaining = new LinkedHashMap<>();
        for (var item : items) {
            var amount = item.lineAmount();
            var vip = amount.multiply(BigDecimal.valueOf(memberDiscountPercent))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
            var config = configurations.stream().filter(p -> p.skuId().equals(item.productId())).findFirst();
            if (config.isPresent()) {
                var value = config.get();
                var discounted = "PRICE".equals(value.discountType())
                        ? amount.subtract(value.discountPrice().multiply(BigDecimal.valueOf(item.quantity())))
                        : amount.multiply(BigDecimal.valueOf(value.discountPercent()))
                            .divide(BigDecimal.valueOf(10000), 2, RoundingMode.DOWN);
                amount = discounted.max(BigDecimal.ZERO).min(vip);
            } else {
                amount = vip;
            }
            remaining.put(item.productId(), amount);
        }
        // 芋道按最新匹配活动处理满减，不保留自定义优先级或最优活动枚举机制。
        var matched = rewards.enabled().stream().filter(a -> !now.isBefore(a.startTime())
                && now.isBefore(a.endTime()) && items.stream().anyMatch(i -> matches(a, i.productId()))).findFirst();
        if (matched.isPresent()) {
            var activity = matched.get();
            var eligible = new LinkedHashMap<Long, BigDecimal>();
            items.stream().filter(i -> matches(activity, i.productId()))
                    .forEach(i -> eligible.put(i.productId(), remaining.get(i.productId())));
            var basis = "COUNT".equals(activity.conditionType())
                    ? BigDecimal.valueOf(items.stream().filter(i -> eligible.containsKey(i.productId()))
                        .mapToInt(SettlementItem::quantity).sum())
                    : total(eligible);
            var rule = activity.rules().stream().filter(r -> basis.compareTo(r.limit()) >= 0)
                    .max(java.util.Comparator.comparing(r -> r.limit()));
            if (rule.isPresent()) {
                subtract(remaining, eligible, rule.get().discountPrice().min(total(eligible)));
            }
        }
        if (coupon != null) {
            if (!"AVAILABLE".equals(coupon.status())) {
                throw new ServiceException("优惠券不可用", 400);
            }
            var terms = coupon.terms();
            if (terms == null) {
                throw new ServiceException("优惠券规则尚未完成数据迁移", 409);
            }
            var eligible = new LinkedHashMap<Long, BigDecimal>();
            items.stream().filter(i -> matches(terms.productScope(), terms.productScopeValues(), i.productId()))
                    .forEach(i -> eligible.put(i.productId(), remaining.get(i.productId())));
            if (eligible.isEmpty()) {
                throw new ServiceException("商品不在优惠券适用范围内", 400);
            }
            subtract(remaining, eligible, CouponRules.discount(terms, total(eligible), now));
        }
        var config = memberTradeGateway.config();
        long usePoint = 0L;
        if (pointStatus && config.enabled() && config.unitPrice() > 0 && points > 0) {
            usePoint = config.maxPoints() > 0 ? Math.min(points, config.maxPoints()) : points;
            var deduction = BigDecimal.valueOf(usePoint).multiply(BigDecimal.valueOf(config.unitPrice()))
                    .movePointLeft(2);
            if (deduction.compareTo(total(remaining)) >= 0) {
                throw new ServiceException("积分抵扣后实付必须大于零", 400);
            }
            subtract(remaining, new LinkedHashMap<>(remaining), deduction);
        }
        var paid = total(remaining);
        long givePoint = config.enabled() && config.givePerYuan() > 0
                ? paid.multiply(BigDecimal.valueOf(config.givePerYuan())).setScale(0,
                    RoundingMode.DOWN).longValueExact()
                : 0L;
        var pricedItems = items.stream().map(i -> new SettlementItem(i.productId(),
                i.productCode(),
                i.productName(),
                i.unitPrice(),
                i.quantity(),
                i.lineAmount(),
                remaining.get(i.productId()),
                java.math.BigDecimal.ZERO.setScale(2),
                i.spuId(),
                i.spuName(),
                i.properties(),
                i.picUrl())).toList();
        return new SettlementQuote(pricedItems, original, original.subtract(paid), paid,
                coupon == null ? null : coupon.id(), usePoint, givePoint);
    }

    /** 范围使用商品主体和所属分类，不能把 SKU 编号当作 SPU。 */
    private boolean matches(RewardActivity activity, Long skuId) {
        return matches(activity.productScope(), activity.productScopeValues(), skuId);
    }

    /** 券与满减均以 SPU 或分类编号匹配商品范围。 */
    private boolean matches(String scope, List<Long> values, Long skuId) {
        if ("ALL".equals(scope)) {
            return true;
        }
        var sku = skus.catalogSku(skuId);
        if (sku == null || sku.spuId() == null) {
            return false;
        }
        if ("SPU_IDS".equals(scope)) {
            return values.contains(sku.spuId());
        }
        var spu = spus.findSpu(sku.spuId());
        return spu != null && values.contains(spu.categoryId());
    }

    private BigDecimal total(Map<Long, BigDecimal> amounts) {
        return amounts.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** 按行金额累计分摊，精确到分，最后一行承担尾差，合计严格等于优惠额。 */
    private void subtract(Map<Long, BigDecimal> target, Map<Long, BigDecimal> weights, BigDecimal discount) {
        var total = total(weights);
        if (discount.signum() == 0) {
            return;
        }
        if (total.signum() <= 0 || discount.compareTo(total) > 0) {
            throw new IllegalStateException("优惠金额超过商品实付");
        }
        var cumulative = BigDecimal.ZERO;
        var allocated = BigDecimal.ZERO;
        for (var entry : weights.entrySet()) {
            cumulative = cumulative.add(entry.getValue());
            var amount = discount.multiply(cumulative).divide(total, 2, RoundingMode.DOWN);
            target.put(entry.getKey(), target.get(entry.getKey()).subtract(amount.subtract(allocated)));
            allocated = amount;
        }
    }
}
