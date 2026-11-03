package com.lemnt.trade.application.aftersale.service;

import com.lemnt.trade.domain.aftersale.model.AfterSale;
import com.lemnt.trade.domain.aftersale.service.AfterSaleRefundCalculator;
import com.lemnt.trade.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.promotion.gateway.OrderPromotionGateway;
import com.lemnt.trade.domain.promotion.model.OrderPromotionRefund;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** 在创建事务内冻结优惠归因及积分，执行时只读取，不再计算。 */
@Component
@RequiredArgsConstructor
public class AfterSalePromotionService {
    private final TradeOrderItemGateway items;
    private final OrderPromotionGateway promotions;

    /** 只对有完整来源的订单生成完整归因，历史未知不推测。 */
    public Plan freeze(
            TradeOrder order,
            Long itemId,
            int quantity,
            int refundedQuantity,
            List<AfterSale> history) {
        var item =
                items.items(order.id()).stream()
                        .filter(i -> itemId.equals(i.orderItemId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("原订单明细不存在"));
        if (item.usePoint() == null || item.givePoint() == null) {
            throw new IllegalStateException("原行积分快照不完整");
        }
        var sources =
                promotions.find(order.id()).stream()
                        .flatMap(p -> p.items().stream())
                        .filter(p -> itemId.equals(p.orderItemId()))
                        .toList();
        var previous =
                promotions.refunds(
                        history.stream()
                                .filter(h -> "REFUNDED".equals(h.status()))
                                .map(AfterSale::id)
                                .filter(Objects::nonNull)
                                .toList());
        long use =
                AfterSaleRefundCalculator.points(
                        item.usePoint(), item.quantity(), refundedQuantity, quantity);
        long give =
                AfterSaleRefundCalculator.points(
                        item.givePoint(), item.quantity(), refundedQuantity, quantity);
        long useRemaining =
                sources.stream()
                        .mapToLong(
                                s ->
                                        s.usePoint()
                                                - previous.stream()
                                                        .filter(
                                                                p ->
                                                                        s.id().equals(
                                                                                        p
                                                                                                .orderItemPromotionId()))
                                                        .mapToLong(
                                                                OrderPromotionRefund
                                                                        ::returnUsePoint)
                                                        .sum())
                        .sum();
        long giveRemaining =
                sources.stream()
                        .mapToLong(
                                s ->
                                        s.givePoint()
                                                - previous.stream()
                                                        .filter(
                                                                p ->
                                                                        s.id().equals(
                                                                                        p
                                                                                                .orderItemPromotionId()))
                                                        .mapToLong(
                                                                OrderPromotionRefund
                                                                        ::deductGivePoint)
                                                        .sum())
                        .sum();
        if (use > useRemaining || give > giveRemaining) {
            throw new IllegalStateException("原行积分归因额度不一致");
        }
        var rows = new ArrayList<OrderPromotionRefund>();
        long used = 0, given = 0, useBasis = 0, giveBasis = 0;
        var discount = BigDecimal.ZERO.setScale(2);
        var freight = BigDecimal.ZERO.setScale(2);
        for (var source : sources) {
            var old =
                    previous.stream()
                            .filter(p -> source.id().equals(p.orderItemPromotionId()))
                            .toList();
            var d =
                    AfterSaleRefundCalculator.cash(
                            source.discountAmount(),
                            item.quantity(),
                            refundedQuantity,
                            old.stream()
                                    .map(OrderPromotionRefund::refundDiscountAmount)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add),
                            quantity);
            var f =
                    AfterSaleRefundCalculator.cash(
                            source.deliveryDiscountPrice(),
                            item.quantity(),
                            refundedQuantity,
                            old.stream()
                                    .map(OrderPromotionRefund::refundDeliveryDiscountPrice)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add),
                            quantity);
            long ub =
                    source.usePoint()
                            - old.stream().mapToLong(OrderPromotionRefund::returnUsePoint).sum();
            long gb =
                    source.givePoint()
                            - old.stream().mapToLong(OrderPromotionRefund::deductGivePoint).sum();
            if (ub < 0 || gb < 0) {
                throw new IllegalStateException("来源积分已退超额");
            }
            useBasis = Math.addExact(useBasis, ub);
            giveBasis = Math.addExact(giveBasis, gb);
            long nextUse = proportional(use, useBasis, useRemaining);
            long nextGive = proportional(give, giveBasis, giveRemaining);
            rows.add(
                    new OrderPromotionRefund(
                            null, null, source.id(), d, f, nextUse - used, nextGive - given));
            used = nextUse;
            given = nextGive;
            discount = discount.add(d);
            freight = freight.add(f);
        }
        if (used != use || given != give) {
            throw new IllegalStateException("积分归因合计不一致");
        }
        return new Plan(discount, freight, use, give, rows);
    }

    /** 保存已生成的明细结果，不再次运行规则。 */
    public void save(Long afterSaleId, Plan plan) {
        promotions.saveRefund(afterSaleId, plan.rows());
    }

    private static long proportional(long target, long cumulative, long total) {
        return total == 0
                ? 0
                : BigDecimal.valueOf(target)
                        .multiply(BigDecimal.valueOf(cumulative))
                        .divide(BigDecimal.valueOf(total), 0, RoundingMode.DOWN)
                        .longValueExact();
    }

    /** 创建事务中的暂存结果，积分数表示应执行值，非已到账事实。 */
    public record Plan(
            BigDecimal discount,
            BigDecimal freight,
            long use,
            long give,
            List<OrderPromotionRefund> rows) {}
}
