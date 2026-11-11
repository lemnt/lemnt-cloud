package com.lemnt.member.infrastructure.event.gateway;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.member.domain.event.gateway.InboundEventGateway;
import com.lemnt.member.domain.event.model.InboundEventFacts;
import com.lemnt.order.client.dto.OrderStockLineDTO;
import com.lemnt.order.client.event.AfterSaleCompletedEvent;
import com.lemnt.order.client.event.OrderClosedEvent;
import com.lemnt.order.client.event.OrderPaidEvent;
import com.lemnt.order.client.event.OrderShippedEvent;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

/** 外部订单协议只在基础设施层解码，业务校验仍由会员事件处理器负责。 */
@Component
public class InboundEventGatewayImpl implements InboundEventGateway {
    @Override
    public InboundEventFacts.Paid paidFacts(String payload) {
        var json = payloadObject(payload);
        requireFields(json, "orderNo", "memberId", "amount", "usePoint", "givePoint");
        requireLineQuantities(json);
        var event = JsonUtils.toBean(payload, OrderPaidEvent.class);
        return new InboundEventFacts.Paid(
            event.orderNo(),
            event.memberId(),
            event.amount(),
            event.usePoint(),
            event.givePoint(),
            event.giveCouponTemplateCounts(),
            lines(event.items()));
    }

    @Override
    public InboundEventFacts.Closed closedFacts(String payload) {
        var json = payloadObject(payload);
        requireFields(json, "orderNo", "memberId");
        requireLineQuantities(json);
        var event = JsonUtils.toBean(payload, OrderClosedEvent.class);
        return new InboundEventFacts.Closed(event.orderNo(), event.memberId(), lines(event.items()));
    }

    @Override
    public InboundEventFacts.AfterSaleCompleted afterSaleFacts(String payload) {
        var json = payloadObject(payload);
        requireFields(
            json,
            "orderNo",
            "afterSaleNo",
            "memberId",
            "quantity",
            "amount",
            "returnUsePoint",
            "deductGivePoint",
            "ruleVersion",
            "fullyRefunded");
        var event = JsonUtils.toBean(payload, AfterSaleCompletedEvent.class);
        return new InboundEventFacts.AfterSaleCompleted(
            event.orderNo(),
            event.afterSaleNo(),
            event.memberId(),
            event.orderItemId(),
            event.skuId(),
            event.quantity(),
            event.amount(),
            event.returnUsePoint(),
            event.deductGivePoint(),
            event.ruleVersion(),
            event.fullyRefunded());
    }

    @Override
    public InboundEventFacts.Shipped shippedFacts(String payload) {
        requireFields(payloadObject(payload), "orderNo", "memberId");
        var event = JsonUtils.toBean(payload, OrderShippedEvent.class);
        return new InboundEventFacts.Shipped(event.orderNo(), event.memberId());
    }

    private JSONObject payloadObject(String payload) {
        var json = JsonUtils.toBean(payload, JSONObject.class);
        if (json == null) {
            throw new IllegalArgumentException("订单事件载荷不能为空");
        }
        return json;
    }

    private void requireFields(JSONObject json, String... names) {
        for (String name : names) {
            if (json == null || !json.containsKey(name) || json.get(name) == null) {
                throw new IllegalArgumentException("订单事件缺少字段：" + name);
            }
        }
    }

    private void requireLineQuantities(JSONObject json) {
        var items = json.getJSONArray("items");
        if (items == null) return;
        for (int index = 0; index < items.size(); index++) {
            requireFields(items.getJSONObject(index), "quantity");
        }
    }

    private List<InboundEventFacts.StockLine> lines(List<OrderStockLineDTO> items) {
        return items == null
            ? null
            : items.stream()
                .map(item -> new InboundEventFacts.StockLine(item.skuId(), item.quantity()))
                .toList();
    }
}
