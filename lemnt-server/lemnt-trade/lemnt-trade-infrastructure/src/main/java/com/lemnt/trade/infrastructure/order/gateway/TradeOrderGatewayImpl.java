package com.lemnt.trade.infrastructure.order.gateway;

import com.lemnt.trade.domain.fulfillment.model.FulfillmentFilter;
import com.lemnt.trade.domain.fulfillment.model.OrderPage;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.order.model.OrderTotal;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.infrastructure.fulfillment.converter.FulfillmentConverter;
import com.lemnt.trade.infrastructure.order.converter.OrderDeliveryConverter;
import com.lemnt.trade.infrastructure.order.dataobject.TradeOrderDO;
import com.lemnt.trade.infrastructure.order.mapper.TradeOrderMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
        var value = tradeOrderMapper.selectOne(Wrappers.lambdaQuery(TradeOrderDO.class)
                .eq(TradeOrderDO::getOrderNo, orderNo));
        return value == null ? null : value.getRemark();
    }

    @Override
    public boolean updateRemark(String orderNo, String remark) {
        return tradeOrderMapper.update(null, Wrappers.lambdaUpdate(TradeOrderDO.class)
                .eq(TradeOrderDO::getOrderNo, orderNo).set(TradeOrderDO::getRemark, remark)) == 1;
    }

    @Override
    /**
     * 查询订单。
     */
    @Transactional(readOnly = true)
    public OrderPage orders(FulfillmentFilter filter) {
        return new OrderPage(
                tradeOrderMapper.orders(filter).stream().map(FulfillmentConverter::orderSummary).toList(),
                tradeOrderMapper.orderCount(filter));
    }

    @Override
    public LocalDateTime databaseNow() {
        return tradeOrderMapper.databaseNow();
    }

    @Override
    public boolean transitionStatus(String orderNo, String expectedStatus, String targetStatus) {
        return tradeOrderMapper.transitionStatus(orderNo, expectedStatus, targetStatus) == 1;
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
        if (n != 1
        ) {
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
                .setTotalAmount(order.totalAmount()).setUsePoint(order.usePoint()).setGivePoint(order.givePoint())
                .setStatus(order.status());

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
                row.getCreateTime(), row.getUsePoint(), row.getGivePoint(), OrderDeliveryConverter.from(row));
    }

    /** 读取订单主体。 */
    @Override
    public TradeOrder findHeader(String orderNo) {
        TradeOrderDO row =
                tradeOrderMapper.selectOne(
                Wrappers.lambdaQuery(TradeOrderDO.class)
                .eq(TradeOrderDO::getOrderNo, orderNo)
                .last("LIMIT 1"));
        if (row == null
        ) {
            return null;
        }
        return new TradeOrder(
                row.getId(),
                row.getOrderNo(),
                row.getMemberId(),
                row.getTotalAmount(),
                row.getStatus(),
                List.of(),
                row.getCreateTime(), row.getUsePoint(), row.getGivePoint(), OrderDeliveryConverter.from(row));
    }

    /** 锁定订单主体。 */
    @Override
    public TradeOrder lockHeader(String orderNo) {
        // Caller must own a transaction. Lock header before reading payment rows.
        TradeOrderDO row =
                tradeOrderMapper.selectOne(
                Wrappers.lambdaQuery(TradeOrderDO.class)
                .eq(TradeOrderDO::getOrderNo, orderNo)
                .last("FOR UPDATE"));
        if (row == null
        ) {
            return null;
        }
        return new TradeOrder(
                row.getId(),
                row.getOrderNo(),
                row.getMemberId(),
                row.getTotalAmount(),
                row.getStatus(),
                List.of(),
                row.getCreateTime(), row.getUsePoint(), row.getGivePoint(), OrderDeliveryConverter.from(row));
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
                row.getCreateTime(), row.getUsePoint(), row.getGivePoint(), OrderDeliveryConverter.from(row)))
                .toList();
    }
}
