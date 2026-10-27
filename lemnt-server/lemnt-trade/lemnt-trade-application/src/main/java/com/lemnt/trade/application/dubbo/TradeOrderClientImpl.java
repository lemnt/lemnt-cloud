package com.lemnt.trade.application.dubbo;

import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.application.order.service.OrderSubmissionService;
import com.lemnt.trade.client.TradeOrderClient;
import com.lemnt.trade.client.bo.OrderCreateBo;
import com.lemnt.trade.client.dto.OrderDTO;
import com.lemnt.trade.client.dto.OrderItemDTO;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.order.model.TradeOrderItem;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.dao.DuplicateKeyException;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/** 接收商城确认后的缓存快照，交易服务不参与购物车或促销计价。 */
@DubboService
@RequiredArgsConstructor
public class TradeOrderClientImpl implements TradeOrderClient {
    private final OrderSubmissionService submissions;
    private final OrderPersistenceService orders;

    @Override
    public OrderDTO create(OrderCreateBo value) {
        if (value == null || value.orderNo() == null || !value.orderNo().matches("[A-Za-z0-9_-]{1,40}")
                || value.memberId() == null || value.memberId() <= 0 || value.totalAmount() == null
                || value.totalAmount().signum() <= 0 || value.totalAmount().scale() > 2
                || value.items() == null || value.items().isEmpty() || value.items().size() > 100) {
            throw new IllegalArgumentException("订单快照不合法");
        }
        for (var item : value.items()) {
            if (item == null || item.productId() == null || item.productId() <= 0
                    || item.quantity() == null || item.quantity() < 1 || item.quantity() > 999
                    || item.unitPrice() == null || item.unitPrice().signum() <= 0
                    || item.unitPrice().scale() > 2 || item.lineAmount() == null
                    || item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()))
                            .compareTo(item.lineAmount()) != 0) {
                throw new IllegalArgumentException("订单明细快照不合法");
            }
        }
        var order = new TradeOrder(null, value.orderNo(), value.memberId(), value.totalAmount(),
                "RESERVING", value.items().stream().map(item -> new TradeOrderItem(item.productId(),
                        item.productCode(), item.productName(), item.unitPrice(), item.quantity(),
                        item.lineAmount())).toList(), null);
        try {
            return to(submissions.create(order));
        } catch (DuplicateKeyException exception) {
            // 并发首次提交由订单号唯一键仲裁，失败事务结束后读取已提交结果。
            var previous = orders.findByOrderNo(order.orderNo());
            if (previous == null) {
                throw exception;
            }
            submissions.requireSame(previous, order);
            return to(previous);
        }
    }

    @Override
    public OrderDTO getByOrderNo(String orderNo) {
        return to(orders.findByOrderNo(orderNo));
    }

    private OrderDTO to(TradeOrder value) {
        return value == null ? null : new OrderDTO(value.id(), value.orderNo(), value.memberId(),
                value.totalAmount(), value.status(), value.items().stream().map(item ->
                        new OrderItemDTO(item.productId(), item.productCode(), item.productName(),
                                item.unitPrice(), item.quantity(), item.lineAmount())).toList(), value.createTime());
    }
}
