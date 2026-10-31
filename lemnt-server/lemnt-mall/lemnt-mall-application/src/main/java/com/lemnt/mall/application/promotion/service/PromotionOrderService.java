package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.promotion.cmd.CombinationRecordCreateCmd;
import com.lemnt.mall.application.promotion.cmd.PromotionOrderCmd;
import com.lemnt.mall.application.promotion.dto.PromotionRefundResult;
import com.lemnt.mall.client.bo.RefundStockBo;
import com.lemnt.mall.domain.promotion.gateway.PromotionOrderGateway;
import com.lemnt.mall.domain.promotion.model.PromotionOrder;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/** 活动订单下单与生命周期补偿：活动库存预占、恢复、拼团建团。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionOrderService {
    private final PromotionOrderGateway promotionOrders;
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
        String fingerprint = String.join(":", "PROMOTION", String.valueOf(cmd.activityType()),
                String.valueOf(cmd.activityId()), String.valueOf(cmd.recordId()),
                String.valueOf(cmd.headId()), String.valueOf(cmd.skuId()),
                String.valueOf(cmd.quantity()));
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
        switch (value.activityType()) {
            case "SECKILL" -> seckillStock.decreaseStock(value.activityId(), value.skuId(), value.quantity());
            case "POINT" -> pointActivities.decreaseStock(value.activityId(), value.skuId(), value.quantity());
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
            restore(value.activityType(), value.activityId(), value.skuId(), value.quantity());
        }
        if (!promotionOrders.markClosed(orderNo)) {
            throw new IllegalStateException("活动订单状态冲突");
        }
    }

    /** 售后按退款数量恢复活动库存，重复回调按已退数量去重。 */
    @Transactional(rollbackFor = Exception.class)
    public PromotionRefundResult refunded(String orderNo, String refundNo, SettlementOrder order,
            List<RefundStockBo> items) {
        var value = promotionOrders.lockByOrderNo(orderNo);
        if (value == null || !List.of("RESERVED", "PAID").contains(value.status())) {
            return PromotionRefundResult.none();
        }
        require(refundNo != null && !refundNo.isBlank() && refundNo.length() <= 64, "退款单号不合法");
        var requested = items == null ? 0 : items.stream()
                .filter(item -> Objects.equals(item.productId(), value.skuId()))
                .mapToInt(RefundStockBo::quantity).sum();
        if (!promotionOrders.recordRefund(orderNo, refundNo, requested)) {
            return PromotionRefundResult.none();
        }
        if (requested <= 0) {
            return PromotionRefundResult.none();
        }
        var remaining = value.quantity() - value.refundedQuantity();
        var delta = Math.min(requested, remaining);
        var newQuantity = value.refundedQuantity();
        if (delta > 0) {
            restore(value.activityType(), value.activityId(), value.skuId(), delta);
            if (!promotionOrders.addRefundedQuantity(orderNo, value.refundedQuantity(), delta)) {
                throw new IllegalStateException("活动订单退款数量冲突");
            }
            newQuantity += delta;
        }
        if ("REFUNDED".equals(order.status()) && !promotionOrders.markRefunded(orderNo)) {
            var current = promotionOrders.findByOrderNo(orderNo);
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
