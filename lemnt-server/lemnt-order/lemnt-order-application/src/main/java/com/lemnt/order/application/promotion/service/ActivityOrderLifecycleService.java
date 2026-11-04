package com.lemnt.order.application.promotion.service;

import com.lemnt.order.domain.promotion.gateway.PromotionOrderGateway;
import com.lemnt.order.domain.promotion.gateway.PromotionOrderItemGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 订单自己保存活动扩展状态与成功已退数量，营销库存由事件消费方处理。 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ActivityOrderLifecycleService {
    private final PromotionOrderGateway orders;
    private final PromotionOrderItemGateway items;

    public void reserved(String no) {
        var row = orders.lockByOrderNo(no);
        if (row != null && "CREATED".equals(row.status()) && !orders.markReserved(no))
            throw new IllegalStateException("活动预占状态冲突");
    }

    public void paid(String no) {
        var row = orders.lockByOrderNo(no);
        if (row != null && "RESERVED".equals(row.status()) && !orders.markPaid(no))
            throw new IllegalStateException("活动支付状态冲突");
    }

    public void closed(String no) {
        var row = orders.lockByOrderNo(no);
        if (row == null || List.of("CLOSED", "REFUNDED", "FAILED").contains(row.status())) return;
        if ("PAID".equals(row.status())) throw new IllegalStateException("已支付活动订单不能关闭");
        if (!orders.markClosed(no)) throw new IllegalStateException("活动关闭状态冲突");
    }

    public void refunded(String no, String afterSale, Long sku, int quantity, boolean full) {
        var row = orders.lockByOrderNo(no);
        if (row == null) return;
        if (row.refundedQuantity() == null) throw new IllegalStateException("原活动已退数量未知，请先核对迁移");
        if (!orders.recordRefund(no, afterSale, quantity)) return;
        var lines = items.byOrderNo(no);
        if (!lines.isEmpty()) {
            var line =
                    lines.stream()
                            .filter(v -> sku.equals(v.skuId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("原活动商品行缺失"));
            if (line.refundedQuantity() == null
                    || quantity < 1
                    || (long) line.refundedQuantity() + quantity > line.quantity()
                    || !items.increaseRefunded(no, sku, line.refundedQuantity(), quantity))
                throw new IllegalStateException("活动商品已退数量冲突");
        } else if (!sku.equals(row.skuId())) throw new IllegalStateException("历史活动SKU不匹配");
        if ((long) row.refundedQuantity() + quantity > row.quantity()
                || !orders.addRefundedQuantity(no, row.refundedQuantity(), quantity))
            throw new IllegalStateException("活动累计退款数量冲突");
        if (full && !orders.markRefunded(no)) throw new IllegalStateException("活动全退状态冲突");
    }
}
