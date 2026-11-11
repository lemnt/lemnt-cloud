package com.lemnt.product.infrastructure.order.gateway;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.dto.OrderStockLineDTO;
import com.lemnt.order.client.event.AfterSaleCompletedEvent;
import com.lemnt.order.client.event.OrderClosedEvent;
import com.lemnt.order.client.event.OrderPaidEvent;
import com.lemnt.product.domain.order.gateway.OrderEventFactsGateway;
import com.lemnt.product.domain.order.model.AfterSaleCompletedFacts;
import com.lemnt.product.domain.order.model.OrderClosedFacts;
import com.lemnt.product.domain.order.model.OrderPaidFacts;
import com.lemnt.product.domain.order.model.OrderStockLine;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 入站订单事件解析：唯一允许触碰order-client事件契约的位置（外部client只在基础设施层）。
 */
@Component
public class OrderEventFactsGatewayImpl implements OrderEventFactsGateway {
    @Override
    public OrderPaidFacts paidFacts(String payload) {
        var event = JsonUtils.toBean(payload, OrderPaidEvent.class);
        if (event == null) {
            throw new IllegalArgumentException("订单事实不能为空");
        }
        return new OrderPaidFacts(event.orderNo(), lines(event));
    }

    @Override
    public OrderClosedFacts closedFacts(String payload) {
        var event = JsonUtils.toBean(payload, OrderClosedEvent.class);
        if (event == null) {
            throw new IllegalArgumentException("关单事实不能为空");
        }
        return new OrderClosedFacts(event.orderNo(), lines(event));
    }

    @Override
    public AfterSaleCompletedFacts afterSaleFacts(String payload) {
        var event = JsonUtils.toBean(payload, AfterSaleCompletedEvent.class);
        if (event == null) {
            throw new IllegalArgumentException("售后事实不能为空");
        }
        return new AfterSaleCompletedFacts(
            event.orderNo(),
            event.afterSaleNo(),
            event.skuId(),
            event.quantity(),
            event.ruleVersion());
    }

    private static List<OrderStockLine> lines(OrderPaidEvent event) {
        return toLines(event.items());
    }

    private static List<OrderStockLine> lines(OrderClosedEvent event) {
        return toLines(event.items());
    }

    private static List<OrderStockLine> toLines(List<OrderStockLineDTO> items) {
        return items == null
            ? List.of()
            : items.stream().map(i -> new OrderStockLine(i.skuId(), i.quantity())).toList();
    }
}
