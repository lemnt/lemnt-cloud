package com.lemnt.trade.domain.fulfillment.model;

import com.lemnt.trade.domain.order.model.OrderRefundItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 将订单实付金额按商品原价权重分摊，固定主键顺序且尾差归后续商品。 */
public final class RefundAllocation {
    private RefundAllocation() {
    }

    /** 分摊只使用原订单不可变快照，结果金额合计严格等于实付金额。 */
    public static Map<Long, BigDecimal> allocate(BigDecimal paid, List<OrderRefundItem> items) {
        var ordered = items.stream().sorted(Comparator.comparing(OrderRefundItem::id)).toList();
        if (ordered.stream().allMatch(item -> item.payAmount() != null)
        ) {
            var total = ordered.stream().map(OrderRefundItem::payAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (paid == null || total.compareTo(paid) != 0
                    || ordered.stream().anyMatch(item -> item.payAmount().signum() < 0
                        || item.deliveryPrice() == null || item.deliveryPrice().signum() < 0
                        || item.payAmount().compareTo(item.lineAmount().add(item.deliveryPrice())) > 0)
            ) {
                throw new IllegalStateException("商品行实付金额与订单不一致");
            }
            var result = new LinkedHashMap<Long, BigDecimal>();
            ordered.forEach(item -> result.put(item.id(), item.payAmount()));
            return result;
        }
        var gross = ordered.stream().map(OrderRefundItem::lineAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (paid == null || paid.signum() <= 0 || gross.signum() <= 0 || paid.compareTo(gross) > 0
        ) {
            throw new IllegalStateException("订单实付与商品快照不一致，不能分摊退款额度");
        }
        var result = new LinkedHashMap<Long, BigDecimal>();
        var weight = BigDecimal.ZERO;
        var allocated = BigDecimal.ZERO;
        for (var item : ordered
        ) {
            weight = weight.add(item.lineAmount());
            var cumulative = paid.multiply(weight).divide(gross, 2, RoundingMode.DOWN);
            result.put(item.id(), cumulative.subtract(allocated));
            allocated = cumulative;
        }
        return result;
    }
}
