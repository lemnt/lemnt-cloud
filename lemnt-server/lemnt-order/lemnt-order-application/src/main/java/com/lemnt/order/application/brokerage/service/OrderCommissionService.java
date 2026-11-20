package com.lemnt.order.application.brokerage.service;

import com.lemnt.order.domain.aftersale.model.AfterSale;
import com.lemnt.order.domain.brokerage.gateway.OrderCommissionLineGateway;
import com.lemnt.order.domain.brokerage.gateway.ProductCommissionGateway;
import com.lemnt.order.domain.brokerage.model.OrderCommissionLine;
import com.lemnt.order.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.order.domain.order.model.TradeOrderItem;
import com.lemnt.order.domain.settlement.model.SettlementRules;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 独立分佣订单的建单配置、实际入账值和按行退款口径。 */
@Component
@RequiredArgsConstructor
public class OrderCommissionService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final ProductCommissionGateway products;
    private final TradeOrderItemGateway orderItems;
    private final OrderCommissionLineGateway snapshots;

    /** 新订单首次建单时冻结固定配置。含固定行的订单保存全部行，支持混合分佣。 */
    @Transactional(propagation = Propagation.MANDATORY)
    public void capture(Long orderId, List<TradeOrderItem> items) {
        var bySku =
                products.fixedCommissions(
                        items.stream().map(TradeOrderItem::productId).distinct().toList());
        if (bySku.isEmpty()) {
            return;
        }
        var first = ZERO;
        var second = ZERO;
        for (var item : orderItems.items(orderId)) {
            if (item.payAmount() == null) {
                throw new IllegalStateException("独立分佣订单缺少逐行实付快照");
            }
            var fixed = bySku.get(item.productId());
            if (fixed != null) {
                var quantity = BigDecimal.valueOf(item.quantity());
                first = first.add(fixed.first().multiply(quantity));
                second = second.add(fixed.second().multiply(quantity));
            }
            snapshots.insert(
                    new OrderCommissionLine(orderId, item.orderItemId(), fixed, null, null));
        }
        SettlementRules.money(first, true);
        SettlementRules.money(second, true);
    }

    /** 无佣金行快照的历史订单和纯全局订单沿用既有计算。 */
    public List<OrderCommissionLine> lines(Long orderId) {
        return snapshots.lines(orderId);
    }

    /** 按商品行计算支付确认时的实发佣金，金额统一保留两位小数。 */
    public Map<Long, BigDecimal> amounts(
            List<OrderCommissionLine> lines,
            List<TradeOrderItem> items,
            int level,
            Integer percent) {
        if (lines.size() != items.size()) {
            throw new IllegalStateException("佣金快照与订单商品行不一致");
        }
        var byId =
                items.stream()
                        .collect(
                                Collectors.toMap(TradeOrderItem::orderItemId, Function.identity()));
        var result = new LinkedHashMap<Long, BigDecimal>();
        for (var line : lines) {
            var item = byId.get(line.orderItemId());
            if (item == null || item.payAmount() == null) {
                throw new IllegalStateException("佣金快照缺少原订单行实付");
            }
            var fixed = line.fixed();
            var amount =
                    fixed == null
                            ? percentage(item.payAmount(), percent)
                            : (level == 1 ? fixed.first() : fixed.second())
                                    .multiply(BigDecimal.valueOf(item.quantity()));
            result.put(line.orderItemId(), amount);
        }
        return result;
    }

    /** 冻结实发金额，与佣金余额变更在同一事务内提交。 */
    public void recordEarned(Map<Long, BigDecimal> amounts, int level) {
        amounts.forEach(
                (id, amount) -> {
                    if (!snapshots.recordEarned(id, level, amount)) {
                        throw new IllegalStateException("订单行佣金已入账或快照缺失");
                    }
                });
    }

    /** 固定部分按累计已退款件数扣回，全局比例部分按原行累计实退款扣回。 */
    public BigDecimal recalled(
            List<OrderCommissionLine> lines,
            List<TradeOrderItem> items,
            List<AfterSale> successfulRefunds,
            int level) {
        if (lines.size() != items.size()) {
            throw new IllegalStateException("佣金退款缺少原行快照");
        }
        var byId =
                items.stream()
                        .collect(
                                Collectors.toMap(TradeOrderItem::orderItemId, Function.identity()));
        var byItem =
                successfulRefunds.stream().collect(Collectors.groupingBy(AfterSale::orderItemId));
        var sum = ZERO;
        for (var line : lines) {
            var earned = line.earned(level);
            var item = byId.get(line.orderItemId());
            if (item == null || item.payAmount() == null || earned == null) {
                throw new IllegalStateException("佣金退款缺少原行入账事实");
            }
            var refunds = byItem.getOrDefault(line.orderItemId(), List.of());
            if (refunds.isEmpty() || earned.signum() == 0) {
                continue;
            }
            var basis =
                    line.fixed() == null
                            ? refunds.stream().map(AfterSale::amount).reduce(ZERO, BigDecimal::add)
                            : BigDecimal.valueOf(
                                    refunds.stream().mapToLong(AfterSale::quantity).sum());
            var total =
                    line.fixed() == null ? item.payAmount() : BigDecimal.valueOf(item.quantity());
            if (total.signum() <= 0 || basis.signum() < 0 || basis.compareTo(total) > 0) {
                throw new IllegalStateException("原行佣金退款超出订单范围");
            }
            sum = sum.add(earned.multiply(basis).divide(total, 2, RoundingMode.DOWN));
        }
        return sum;
    }

    /** 沿用现有订单的百分比精度和向下取整规则。 */
    public static BigDecimal percentage(BigDecimal amount, Integer percent) {
        return percent == null || percent <= 0
                ? ZERO
                : amount.multiply(BigDecimal.valueOf(percent))
                        .movePointLeft(2)
                        .setScale(2, RoundingMode.DOWN);
    }
}
