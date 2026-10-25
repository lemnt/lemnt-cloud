package com.lemnt.trade.infrastructure.gateway;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lemnt.trade.domain.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.model.TradeOrder;
import com.lemnt.trade.domain.model.TradeOrderItem;
import com.lemnt.trade.infrastructure.dataobject.TradeOrderDO;
import com.lemnt.trade.infrastructure.dataobject.TradeOrderItemDO;
import com.lemnt.trade.infrastructure.mapper.TradeOrderItemMapper;
import com.lemnt.trade.infrastructure.mapper.TradeOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TradeOrderGatewayImpl implements TradeOrderGateway {
    private final TradeOrderMapper orders;
    private final TradeOrderItemMapper items;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder save(TradeOrder order) {
        TradeOrderDO row = new TradeOrderDO().setOrderNo(order.orderNo()).setMemberId(order.memberId())
                .setTotalAmount(order.totalAmount()).setStatus(order.status());
        orders.insert(row);
        for (TradeOrderItem item : order.items()) {
            items.insert(new TradeOrderItemDO().setOrderId(row.getId()).setProductId(item.productId())
                    .setProductCode(item.productCode()).setProductName(item.productName())
                    .setUnitPrice(item.unitPrice()).setQuantity(item.quantity()).setLineAmount(item.lineAmount()));
        }
        return new TradeOrder(row.getId(), row.getOrderNo(), row.getMemberId(), row.getTotalAmount(),
                row.getStatus(), order.items(), row.getCreateTime());
    }

    @Override
    public TradeOrder findByOrderNo(String orderNo) {
        TradeOrderDO row = orders.selectOne(Wrappers.lambdaQuery(TradeOrderDO.class)
                .eq(TradeOrderDO::getOrderNo, orderNo).last("LIMIT 1"));
        if (row == null) return null;
        List<TradeOrderItem> lines = items.selectList(Wrappers.lambdaQuery(TradeOrderItemDO.class)
                .eq(TradeOrderItemDO::getOrderId, row.getId()).orderByAsc(TradeOrderItemDO::getId))
                .stream().map(item -> new TradeOrderItem(item.getProductId(), item.getProductCode(),
                        item.getProductName(), item.getUnitPrice(), item.getQuantity(), item.getLineAmount())).toList();
        return new TradeOrder(row.getId(), row.getOrderNo(), row.getMemberId(), row.getTotalAmount(),
                row.getStatus(), lines, row.getCreateTime());
    }

    @Override
    public TradeOrder findByOrderNoForUpdate(String orderNo) {
        // Caller must own a transaction. Lock header before reading payment rows.
        TradeOrderDO row = orders.selectOne(Wrappers.lambdaQuery(TradeOrderDO.class)
                .eq(TradeOrderDO::getOrderNo, orderNo).last("FOR UPDATE"));
        if (row == null) return null;
        return new TradeOrder(row.getId(), row.getOrderNo(), row.getMemberId(), row.getTotalAmount(),
                row.getStatus(), List.of(), row.getCreateTime());
    }

    @Override
    public boolean transitionStatus(String orderNo, String expectedStatus, String targetStatus) {
        return orders.transitionStatus(orderNo, expectedStatus, targetStatus) == 1;
    }

    @Override
    public List<TradeOrder> findPendingPaymentCreatedBefore(LocalDateTime deadline, int limit) {
        return orders.selectList(Wrappers.lambdaQuery(TradeOrderDO.class)
                .eq(TradeOrderDO::getStatus, "PENDING_PAYMENT")
                .lt(TradeOrderDO::getCreateTime, deadline)
                .orderByAsc(TradeOrderDO::getCreateTime).last("LIMIT " + Math.max(1, Math.min(limit, 500))))
                .stream().map(row -> new TradeOrder(row.getId(), row.getOrderNo(), row.getMemberId(),
                        row.getTotalAmount(), row.getStatus(), List.of(), row.getCreateTime())).toList();
    }
}