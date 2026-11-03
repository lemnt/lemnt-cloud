package com.lemnt.order.infrastructure.order.gateway;

import com.lemnt.order.domain.fulfillment.model.FulfillmentFilter;
import com.lemnt.order.domain.fulfillment.model.OrderPage;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.model.OrderCommentState;
import com.lemnt.order.domain.order.model.OrderStatusCount;
import com.lemnt.order.domain.order.model.OrderTotal;
import com.lemnt.order.domain.order.model.TradeOrder;
import com.lemnt.order.infrastructure.fulfillment.converter.FulfillmentConverter;
import com.lemnt.order.infrastructure.order.converter.OrderDeliveryConverter;
import com.lemnt.order.infrastructure.order.dataobject.TradeOrderDO;
import com.lemnt.order.infrastructure.order.mapper.TradeOrderMapper;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** TradeOrder 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class TradeOrderGatewayImpl implements TradeOrderGateway {
    private final TradeOrderMapper tradeOrderMapper;

    @Override
    public OrderTotal total(String orderNo) {
        var value = tradeOrderMapper.total(orderNo);
        return new OrderTotal(value.orderCount(), value.orderPayPrice());
    }

    @Override
    public String remark(String orderNo) {
        var value =
                tradeOrderMapper.selectOne(
                        Wrappers.lambdaQuery(TradeOrderDO.class)
                                .eq(TradeOrderDO::getOrderNo, orderNo));
        return value == null ? null : value.getRemark();
    }

    @Override
    public boolean updateRemark(String orderNo, String remark) {
        return tradeOrderMapper.update(
                        null,
                        Wrappers.lambdaUpdate(TradeOrderDO.class)
                                .eq(TradeOrderDO::getOrderNo, orderNo)
                                .set(TradeOrderDO::getRemark, remark))
                == 1;
    }

    @Override
    /**
     * 查询订单。
     */
    @Transactional(readOnly = true)
    public OrderPage orders(FulfillmentFilter filter) {
        return new OrderPage(
                tradeOrderMapper.orders(filter).stream()
                        .map(FulfillmentConverter::orderSummary)
                        .toList(),
                tradeOrderMapper.orderCount(filter));
    }

    @Override
    public LocalDateTime databaseNow() {
        return tradeOrderMapper.databaseNow();
    }

    /** 按状态与评价状态统计会员本人订单数量。 */
    @Override
    public List<OrderStatusCount> memberStatusCounts(Long memberId) {
        return tradeOrderMapper.memberStatusCounts(memberId).stream()
                .map(
                        row ->
                                new OrderStatusCount(
                                        row.getStatus(),
                                        row.getCommentStatus(),
                                        row.getDeliveryType(),
                                        row.getCnt()))
                .toList();
    }

    @Override
    public List<String> commentExpired(LocalDateTime cutoff, int limit) {
        return tradeOrderMapper
                .selectList(
                        expired(cutoff)
                                .orderByAsc(TradeOrderDO::getReceiveTime)
                                .orderByAsc(TradeOrderDO::getId)
                                .last("LIMIT " + Math.min(100, Math.max(1, limit))))
                .stream()
                .map(TradeOrderDO::getOrderNo)
                .toList();
    }

    @Override
    public boolean commentExpired(String orderNo, LocalDateTime cutoff) {
        return tradeOrderMapper.selectCount(expired(cutoff).eq(TradeOrderDO::getOrderNo, orderNo))
                == 1;
    }

    private LambdaQueryWrapper<TradeOrderDO> expired(LocalDateTime cutoff) {
        return Wrappers.lambdaQuery(TradeOrderDO.class)
                .eq(TradeOrderDO::getStatus, "COMPLETED")
                .eq(TradeOrderDO::getCommentStatus, false)
                .lt(TradeOrderDO::getReceiveTime, cutoff);
    }

    /** 收货时间与完成状态同一条条件更新，不覆盖已有收货事实。 */
    @Override
    public boolean completeReceipt(String orderNo, LocalDateTime receivedAt) {
        return tradeOrderMapper.update(
                        null,
                        Wrappers.lambdaUpdate(TradeOrderDO.class)
                                .eq(TradeOrderDO::getOrderNo, orderNo)
                                .eq(TradeOrderDO::getStatus, "SHIPPED")
                                .isNull(TradeOrderDO::getReceiveTime)
                                .set(TradeOrderDO::getStatus, "COMPLETED")
                                .set(TradeOrderDO::getReceiveTime, receivedAt)
                                .set(TradeOrderDO::getUpdateTime, receivedAt))
                == 1;
    }

    @Override
    public boolean transitionStatus(String orderNo, String expectedStatus, String targetStatus) {
        return tradeOrderMapper.transitionStatus(orderNo, expectedStatus, targetStatus) == 1;
    }

    @Override
    public boolean updateTotalAmount(String orderNo, BigDecimal expected, BigDecimal next) {
        var current = findHeader(orderNo);
        boolean complete = current != null && "COMPLETE".equals(current.pricingSnapshotStatus());
        return tradeOrderMapper.update(
                        null,
                        Wrappers.lambdaUpdate(TradeOrderDO.class)
                                .eq(TradeOrderDO::getOrderNo, orderNo)
                                .eq(TradeOrderDO::getStatus, "PENDING_PAYMENT")
                                .eq(TradeOrderDO::getTotalAmount, expected)
                                .set(TradeOrderDO::getTotalAmount, next)
                                .set(
                                        complete,
                                        TradeOrderDO::getAdjustmentAmount,
                                        complete
                                                ? current.adjustmentAmount()
                                                        .add(next.subtract(expected))
                                                : null)
                                .set(
                                        complete,
                                        TradeOrderDO::getPricingSnapshotVersion,
                                        complete ? current.pricingSnapshotVersion() + 1 : null))
                == 1;
    }

    @Override
    public boolean updateAddress(
            String orderNo,
            String receiverName,
            String receiverMobile,
            Long receiverAreaId,
            String receiverDetailAddress) {
        return tradeOrderMapper.update(
                        null,
                        Wrappers.lambdaUpdate(TradeOrderDO.class)
                                .eq(TradeOrderDO::getOrderNo, orderNo)
                                .in(TradeOrderDO::getStatus, "PENDING_PAYMENT", "PAID")
                                .eq(TradeOrderDO::getDeliveryType, 1)
                                .set(TradeOrderDO::getReceiverName, receiverName)
                                .set(TradeOrderDO::getReceiverMobile, receiverMobile)
                                .set(TradeOrderDO::getReceiverAreaId, receiverAreaId)
                                .set(TradeOrderDO::getReceiverDetailAddress, receiverDetailAddress))
                == 1;
    }

    @Override
    public List<TradeOrder> findPendingPaymentCreatedBefore(LocalDateTime deadline, int limit) {
        return expired("PENDING_PAYMENT", deadline, limit);
    }

    @Override
    public List<TradeOrder> findReservingCreatedBefore(LocalDateTime deadline, int limit) {
        return expired("RESERVING", deadline, limit);
    }

    private static void one(int n) {
        if (n != 1) {
            throw new IllegalStateException("Promotion persistence CAS/integrity failure");
        }
    }

    /** 保存订单主体，明细由应用层编排。 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder saveHeader(TradeOrder order) {
        TradeOrderDO row =
                new TradeOrderDO()
                        .setOrderNo(order.orderNo())
                        .setMemberId(order.memberId())
                        .setTotalAmount(order.totalAmount())
                        .setUsePoint(order.usePoint())
                        .setGivePoint(order.givePoint())
                        .setStatus(order.status())
                        .setCommentStatus(false)
                        .setGiveCouponTemplateCounts(
                                JSON.toJSONString(order.giveCouponTemplateCounts()))
                        .setOriginalAmount(order.originalAmount())
                        .setDiscountAmount(order.discountAmount())
                        .setDeliveryOriginalPrice(order.deliveryOriginalPrice())
                        .setDeliveryDiscountPrice(order.deliveryDiscountPrice())
                        .setAdjustmentAmount(order.adjustmentAmount())
                        .setPricingSnapshotStatus(order.pricingSnapshotStatus())
                        .setPricingSnapshotVersion(order.pricingSnapshotVersion());

        // Expiry uses the database clock; do not fill order creation from a possibly different JVM
        // zone.
        row.setCreateTime(tradeOrderMapper.databaseNow());
        row.setUpdateTime(row.getCreateTime());
        OrderDeliveryConverter.fill(order.delivery(), row);
        tradeOrderMapper.insert(row);
        return new TradeOrder(
                row.getId(),
                row.getOrderNo(),
                row.getMemberId(),
                row.getTotalAmount(),
                row.getStatus(),
                order.items(),
                row.getCreateTime(),
                row.getUsePoint(),
                row.getGivePoint(),
                OrderDeliveryConverter.from(row),
                row.getCommentStatus(),
                row.getFinishTime(),
                gifts(row.getGiveCouponTemplateCounts()),
                row.getOriginalAmount(),
                row.getDiscountAmount(),
                row.getDeliveryOriginalPrice(),
                row.getDeliveryDiscountPrice(),
                row.getAdjustmentAmount(),
                row.getPricingSnapshotStatus(),
                row.getPricingSnapshotVersion(),
                List.of());
    }

    /** 读取订单主体。 */
    @Override
    public TradeOrder findHeader(String orderNo) {
        TradeOrderDO row =
                tradeOrderMapper.selectOne(
                        Wrappers.lambdaQuery(TradeOrderDO.class)
                                .eq(TradeOrderDO::getOrderNo, orderNo)
                                .last("LIMIT 1"));
        if (row == null) {
            return null;
        }
        return new TradeOrder(
                row.getId(),
                row.getOrderNo(),
                row.getMemberId(),
                row.getTotalAmount(),
                row.getStatus(),
                List.of(),
                row.getCreateTime(),
                row.getUsePoint(),
                row.getGivePoint(),
                OrderDeliveryConverter.from(row),
                row.getCommentStatus(),
                row.getFinishTime(),
                gifts(row.getGiveCouponTemplateCounts()),
                row.getOriginalAmount(),
                row.getDiscountAmount(),
                row.getDeliveryOriginalPrice(),
                row.getDeliveryDiscountPrice(),
                row.getAdjustmentAmount(),
                row.getPricingSnapshotStatus(),
                row.getPricingSnapshotVersion(),
                List.of());
    }

    /** 锁定订单主体。 */
    @Override
    public TradeOrder lockHeader(String orderNo) {
        // Caller must own a transaction. Lock header before reading payment rows.
        TradeOrderDO row = tradeOrderMapper.lockByOrderNo(orderNo);
        if (row == null) {
            return null;
        }
        return new TradeOrder(
                row.getId(),
                row.getOrderNo(),
                row.getMemberId(),
                row.getTotalAmount(),
                row.getStatus(),
                List.of(),
                row.getCreateTime(),
                row.getUsePoint(),
                row.getGivePoint(),
                OrderDeliveryConverter.from(row),
                row.getCommentStatus(),
                row.getFinishTime(),
                gifts(row.getGiveCouponTemplateCounts()),
                row.getOriginalAmount(),
                row.getDiscountAmount(),
                row.getDeliveryOriginalPrice(),
                row.getDeliveryDiscountPrice(),
                row.getAdjustmentAmount(),
                row.getPricingSnapshotStatus(),
                row.getPricingSnapshotVersion(),
                List.of());
    }

    @Override
    public boolean assignPickUp(String orderNo, Long storeId, String verifyCode) {
        return tradeOrderMapper.update(
                        null,
                        Wrappers.lambdaUpdate(TradeOrderDO.class)
                                .eq(TradeOrderDO::getOrderNo, orderNo)
                                .eq(TradeOrderDO::getStatus, "PAID")
                                .set(TradeOrderDO::getDeliveryType, 2)
                                .set(TradeOrderDO::getPickUpStoreId, storeId)
                                .set(TradeOrderDO::getPickUpVerifyCode, verifyCode))
                == 1;
    }

    @Override
    public TradeOrder findHeaderByPickUpCode(String verifyCode) {
        var row =
                tradeOrderMapper.selectOne(
                        Wrappers.lambdaQuery(TradeOrderDO.class)
                                .eq(TradeOrderDO::getPickUpVerifyCode, verifyCode)
                                .last("LIMIT 1"));
        if (row == null) {
            return null;
        }
        return new TradeOrder(
                row.getId(),
                row.getOrderNo(),
                row.getMemberId(),
                row.getTotalAmount(),
                row.getStatus(),
                List.of(),
                row.getCreateTime(),
                row.getUsePoint(),
                row.getGivePoint(),
                OrderDeliveryConverter.from(row),
                row.getCommentStatus(),
                row.getFinishTime(),
                gifts(row.getGiveCouponTemplateCounts()),
                row.getOriginalAmount(),
                row.getDiscountAmount(),
                row.getDeliveryOriginalPrice(),
                row.getDeliveryDiscountPrice(),
                row.getAdjustmentAmount(),
                row.getPricingSnapshotStatus(),
                row.getPricingSnapshotVersion(),
                List.of());
    }

    @Override
    public boolean completePickUp(String orderNo, LocalDateTime time) {
        return tradeOrderMapper.update(
                        null,
                        Wrappers.lambdaUpdate(TradeOrderDO.class)
                                .eq(TradeOrderDO::getOrderNo, orderNo)
                                .eq(TradeOrderDO::getStatus, "PAID")
                                .isNull(TradeOrderDO::getReceiveTime)
                                .set(TradeOrderDO::getStatus, "COMPLETED")
                                .set(TradeOrderDO::getReceiveTime, time)
                                .set(TradeOrderDO::getUpdateTime, time))
                == 1;
    }

    private List<TradeOrder> expired(String status, LocalDateTime deadline, int limit) {
        return tradeOrderMapper
                .selectList(
                        Wrappers.lambdaQuery(TradeOrderDO.class)
                                .eq(TradeOrderDO::getStatus, status)
                                .lt(TradeOrderDO::getCreateTime, deadline)
                                .orderByAsc(TradeOrderDO::getCreateTime)
                                .last("LIMIT " + Math.max(1, Math.min(limit, 500))))
                .stream()
                .map(
                        row ->
                                new TradeOrder(
                                        row.getId(),
                                        row.getOrderNo(),
                                        row.getMemberId(),
                                        row.getTotalAmount(),
                                        row.getStatus(),
                                        List.of(),
                                        row.getCreateTime(),
                                        row.getUsePoint(),
                                        row.getGivePoint(),
                                        OrderDeliveryConverter.from(row),
                                        row.getCommentStatus(),
                                        row.getFinishTime(),
                                        gifts(row.getGiveCouponTemplateCounts()),
                                        row.getOriginalAmount(),
                                        row.getDiscountAmount(),
                                        row.getDeliveryOriginalPrice(),
                                        row.getDeliveryDiscountPrice(),
                                        row.getAdjustmentAmount(),
                                        row.getPricingSnapshotStatus(),
                                        row.getPricingSnapshotVersion(),
                                        List.of()))
                .toList();
    }

    @Override
    public boolean updateCommentState(OrderCommentState value) {
        return tradeOrderMapper.update(
                        null,
                        Wrappers.lambdaUpdate(TradeOrderDO.class)
                                .eq(TradeOrderDO::getId, value.orderId())
                                .set(TradeOrderDO::getCommentStatus, value.commentStatus())
                                .set(TradeOrderDO::getFinishTime, value.finishTime()))
                == 1;
    }

    /** 商城赠券快照以 JSON 保存，历史订单无该字段时按空赠券读取。 */
    private static Map<Long, Integer> gifts(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        Map<Long, Integer> value =
                JSON.parseObject(json, new TypeReference<Map<Long, Integer>>() {});
        return value == null ? Map.of() : value;
    }
}
