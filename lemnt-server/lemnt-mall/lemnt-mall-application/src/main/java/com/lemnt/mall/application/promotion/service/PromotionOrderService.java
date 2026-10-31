package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.promotion.cmd.CombinationRecordCreateCmd;
import com.lemnt.mall.application.promotion.cmd.PromotionOrderCmd;
import com.lemnt.mall.application.promotion.dto.PromotionRefundResult;
import com.lemnt.mall.client.bo.RefundStockBo;
import com.lemnt.mall.domain.promotion.gateway.PromotionOrderGateway;
import com.lemnt.mall.domain.promotion.gateway.PromotionOrderItemGateway;
import com.lemnt.mall.domain.promotion.model.PromotionOrder;
import com.lemnt.mall.domain.promotion.model.PromotionOrderItem;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** 活动订单下单与生命周期补偿：活动库存预占、恢复、拼团建团。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionOrderService {
    private final PromotionOrderGateway promotionOrders;
    private final PromotionOrderItemGateway promotionOrderItems;
    private final PromotionOrderPreparationService preparation;
    private final OrderGateway orders;
    private final SeckillStockService seckillStock;
    private final BargainActivityManagementService bargainActivities;
    private final PointActivityManagementService pointActivities;
    private final CombinationRecordService combinationRecords;
    private final BargainRecordService bargainRecords;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    /** 本地准备提交后同步调用交易建单，未知结果保留原请求供重试。 */
    public SettlementOrder place(PromotionOrderCmd cmd) {
        require(cmd != null && cmd.memberId() != null && cmd.memberId() > 0, "会员编号不合法");
        require(cmd.requestNo() != null && cmd.requestNo().matches("[A-Za-z0-9_-]{1,64}"),
                "请求编号不合法");
        require(cmd.lines() != null && !cmd.lines().isEmpty(), "活动商品不能为空");
        var lineKey = cmd.lines().stream()
                .map(line -> line.skuId() + ":" + line.quantity()).sorted().toList();
        String fingerprint = cmd.lines().size() == 1
                ? String.join(":", "PROMOTION", String.valueOf(cmd.activityType()),
                    String.valueOf(cmd.activityId()), String.valueOf(cmd.recordId()),
                    String.valueOf(cmd.headId()), String.valueOf(cmd.skuId()),
                    String.valueOf(cmd.quantity()))
                : String.join(":", "PROMOTION", String.valueOf(cmd.activityType()),
                    String.valueOf(cmd.activityId()), String.valueOf(cmd.recordId()),
                    String.valueOf(cmd.headId()), lineKey.toString());
        var prepared = preparation.prepare(cmd, fingerprint);
        try {
            return orders.save(prepared.order());
        } catch (RuntimeException exception) {
            // 交易未生成订单时允许同一请求重试；已生成订单交给 closed 补偿。
            try {
                if (orders.findByOrderNo(prepared.order().orderNo()) == null) {
                    promotionOrders.markFailed(prepared.order().orderNo(),
                            exception.getMessage() == null ? "交易建单失败" : exception.getMessage());
                }
            } catch (RuntimeException probeException) {
                log.warn("活动订单失败状态未能确认, orderNo={}", prepared.order().orderNo(), probeException);
            }
            throw exception;
        }
    }

    /** 交易预占 SKU 库存后同步预占活动库存；重复回调不重复扣减。 */
    @Transactional(rollbackFor = Exception.class)
    public void reserved(String orderNo, SettlementOrder order) {
        var value = promotionOrders.lockByOrderNo(orderNo);
        if (value == null || !"CREATED".equals(value.status())) {
            return;
        }
        var lines = promotionOrderItems.byOrderNo(orderNo);
        switch (value.activityType()) {
            case "SECKILL" -> {
                if (lines.isEmpty()) {
                    seckillStock.decreaseStock(value.activityId(), value.skuId(), value.quantity());
                } else {
                    lines.forEach(line -> seckillStock.decreaseStock(value.activityId(), line.skuId(),
                            line.quantity()));
                }
            }
            case "POINT" -> {
                if (lines.isEmpty()) {
                    pointActivities.decreaseStock(value.activityId(), value.skuId(), value.quantity());
                } else {
                    lines.forEach(line -> pointActivities.decreaseStock(value.activityId(), line.skuId(),
                            line.quantity()));
                }
            }
            case "BARGAIN" -> {
                bargainActivities.decreaseStock(value.activityId(), value.quantity());
                if (order.id() != null && value.activityRecordId() != null) {
                    bargainRecords.bindOrder(value.activityRecordId(), order.id());
                }
            }
            case "COMBINATION" -> { }
            default -> throw new ServiceException("活动类型不合法", 400);
        }
        if (!promotionOrders.markReserved(orderNo)) {
            throw new IllegalStateException("活动订单状态冲突");
        }
    }

    /** 支付成功后拼团建团并绑定订单，所有类型记录为已支付。 */
    @Transactional(rollbackFor = Exception.class)
    public void paid(String orderNo, SettlementOrder order) {
        var value = promotionOrders.lockByOrderNo(orderNo);
        if (value == null || !"RESERVED".equals(value.status())) {
            return;
        }
        if ("COMBINATION".equals(value.activityType()) && value.activityRecordId() == null) {
            var item = order.items().stream()
                    .filter(line -> Objects.equals(line.productId(), value.skuId())).findFirst()
                    .orElseThrow(() -> new IllegalStateException("活动订单商品不存在"));
            var record = combinationRecords.create(new CombinationRecordCreateCmd(value.memberId(),
                    item.spuId(), value.activityId(), value.headId(), value.skuId(), value.quantity()));
            if (order.id() != null) {
                combinationRecords.bindOrder(record.id(), order.id());
            }
            promotionOrders.bindRecord(orderNo, record.id());
        }
        if (!promotionOrders.markPaid(orderNo)) {
            throw new IllegalStateException("活动订单状态冲突");
        }
    }

    /** 未支付订单关闭时恢复活动库存；已支付订单不允许关闭。 */
    @Transactional(rollbackFor = Exception.class)
    public void closed(String orderNo) {
        var value = promotionOrders.lockByOrderNo(orderNo);
        if (value == null || List.of("CLOSED", "REFUNDED", "FAILED").contains(value.status())) {
            return;
        }
        if ("PAID".equals(value.status())) {
            throw new IllegalStateException("已支付活动订单不能关闭");
        }
        if ("RESERVED".equals(value.status())) {
            var lines = promotionOrderItems.byOrderNo(orderNo);
            if (lines.isEmpty()) {
                restore(value.activityType(), value.activityId(), value.skuId(), value.quantity());
            } else {
                lines.forEach(line -> restore(value.activityType(), value.activityId(), line.skuId(),
                        line.quantity()));
            }
        }
        if (!promotionOrders.markClosed(orderNo)) {
            throw new IllegalStateException("活动订单状态冲突");
        }
    }

    /** 售后按商品行退款数量恢复活动库存，并按行累计数量计算本次应退积分。 */
    @Transactional(rollbackFor = Exception.class)
    public PromotionRefundResult refunded(String orderNo, String refundNo, SettlementOrder order,
            List<RefundStockBo> items) {
        var value = promotionOrders.lockByOrderNo(orderNo);
        if (value == null || !List.of("RESERVED", "PAID").contains(value.status())) {
            return PromotionRefundResult.none();
        }
        require(refundNo != null && !refundNo.isBlank() && refundNo.length() <= 64, "退款单号不合法");
        var requestedBySku = items == null ? Map.<Long, Integer>of() : items.stream()
                .collect(Collectors.groupingBy(RefundStockBo::productId,
                        Collectors.summingInt(RefundStockBo::quantity)));
        var requested = requestedBySku.values().stream().mapToInt(Integer::intValue).sum();
        if (!promotionOrders.recordRefund(orderNo, refundNo, requested)) {
            return PromotionRefundResult.none();
        }
        if (requested <= 0) {
            return PromotionRefundResult.none();
        }
        var lines = promotionOrderItems.byOrderNo(orderNo);
        if (lines.isEmpty()) {
            return legacyRefund(value, order, requested);
        }
        long refundPoints = 0L;
        var totalDelta = 0;
        for (var line : lines) {
            var lineRequested = requestedBySku.getOrDefault(line.skuId(), 0);
            var remaining = line.quantity() - line.refundedQuantity();
            var delta = Math.min(lineRequested, remaining);
            if (delta <= 0) {
                continue;
            }
            restore(value.activityType(), value.activityId(), line.skuId(), delta);
            if (!promotionOrderItems.increaseRefunded(orderNo, line.skuId(), line.refundedQuantity(), delta)) {
                throw new IllegalStateException("活动订单商品退款数量冲突");
            }
            var previousPoints = line.usePoint() * line.refundedQuantity() / line.quantity();
            var currentPoints = line.usePoint() * (line.refundedQuantity() + delta) / line.quantity();
            refundPoints += currentPoints - previousPoints;
            totalDelta += delta;
        }
        if (totalDelta == 0) {
            if ("REFUNDED".equals(order.status()) && value.refundedQuantity() >= value.quantity()
                    && !promotionOrders.markRefunded(orderNo)) {
                var current = promotionOrders.findByOrderNo(orderNo);
                if (current == null || !"REFUNDED".equals(current.status())) {
                    throw new IllegalStateException("活动订单状态冲突");
                }
            }
            return PromotionRefundResult.none();
        }
        var newQuantity = value.refundedQuantity() + totalDelta;
        if (!promotionOrders.addRefundedQuantity(orderNo, value.refundedQuantity(), totalDelta)) {
            throw new IllegalStateException("活动订单退款数量冲突");
        }
        if ("REFUNDED".equals(order.status()) && !promotionOrders.markRefunded(orderNo)) {
            var current = promotionOrders.findByOrderNo(orderNo);
            if (current == null || !"REFUNDED".equals(current.status())) {
                throw new IllegalStateException("活动订单状态冲突");
            }
        }
        return new PromotionRefundResult(refundPoints, newQuantity);
    }

    /** 兼容第144批之前没有商品行映射的活动订单。 */
    private PromotionRefundResult legacyRefund(PromotionOrder value, SettlementOrder order, int requested) {
        var remaining = value.quantity() - value.refundedQuantity();
        var delta = Math.min(requested, remaining);
        var newQuantity = value.refundedQuantity();
        if (delta > 0) {
            restore(value.activityType(), value.activityId(), value.skuId(), delta);
            if (!promotionOrders.addRefundedQuantity(value.orderNo(), value.refundedQuantity(), delta)) {
                throw new IllegalStateException("活动订单退款数量冲突");
            }
            newQuantity += delta;
        }
        if ("REFUNDED".equals(order.status()) && !promotionOrders.markRefunded(value.orderNo())) {
            var current = promotionOrders.findByOrderNo(value.orderNo());
            if (current == null || !"REFUNDED".equals(current.status())) {
                throw new IllegalStateException("活动订单状态冲突");
            }
        }
        long previousPoints = value.usePoint() * value.refundedQuantity() / value.quantity();
        long currentPoints = value.usePoint() * newQuantity / value.quantity();
        return new PromotionRefundResult(currentPoints - previousPoints, newQuantity);
    }

    private void restore(String type, Long activityId, Long skuId, int quantity) {
        switch (type) {
            case "SECKILL" -> seckillStock.increaseStock(activityId, skuId, quantity);
            case "POINT" -> pointActivities.increaseStock(activityId, skuId, quantity);
            case "BARGAIN" -> bargainActivities.increaseStock(activityId, quantity);
            case "COMBINATION" -> { }
            default -> throw new ServiceException("活动类型不合法", 400);
        }
    }

    /** 仅测试与排查读取，不参与下单事务。 */
    @Transactional(readOnly = true)
    public PromotionOrder find(String orderNo) {
        return promotionOrders.findByOrderNo(orderNo);
    }
}
