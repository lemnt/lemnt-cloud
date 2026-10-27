package com.lemnt.mall.infrastructure.order.gateway;

import com.lemnt.mall.domain.checkout.model.CheckoutItem;
import com.lemnt.mall.domain.order.gateway.OrderGateway;
import com.lemnt.mall.domain.order.model.OrderSnapshot;
import com.lemnt.trade.client.TradeOrderClient;
import com.lemnt.trade.client.bo.OrderCreateBo;
import com.lemnt.trade.client.dto.OrderDTO;
import com.lemnt.trade.client.dto.OrderItemDTO;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 只通过交易服务契约读写订单，不共享事务或 Mapper。 */
@Component
public class OrderGatewayImpl implements OrderGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private TradeOrderClient client;

    @Override
    public OrderSnapshot save(OrderSnapshot order) {
        return from(client.create(new OrderCreateBo(order.orderNo(), order.memberId(),
                order.totalAmount(), order.items().stream().map(item ->
                        new OrderItemDTO(item.productId(), item.productCode(), item.productName(),
                                item.unitPrice(), item.quantity(), item.lineAmount())).toList())));
    }

    @Override
    public OrderSnapshot findByOrderNo(String orderNo) {
        return from(client.getByOrderNo(orderNo));
    }

    private OrderSnapshot from(OrderDTO value) {
        return value == null ? null : new OrderSnapshot(value.id(), value.orderNo(), value.memberId(),
                value.totalAmount(), value.status(), value.items().stream().map(item ->
                        new CheckoutItem(item.productId(), item.productCode(), item.productName(),
                                item.unitPrice(), item.quantity(), item.lineAmount())).toList(), value.createTime());
    }
}
