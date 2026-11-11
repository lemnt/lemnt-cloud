package com.lemnt.promotion.infrastructure.event.gateway;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.dto.OrderStockLineDTO;
import com.lemnt.order.client.event.AfterSaleCompletedEvent;
import com.lemnt.order.client.event.OrderClosedEvent;
import com.lemnt.order.client.event.OrderCompletedEvent;
import com.lemnt.order.client.event.OrderPaidEvent;
import com.lemnt.product.client.event.StockPreReservedEvent;
import com.lemnt.promotion.domain.event.gateway.InboundEventGateway;
import com.lemnt.promotion.domain.event.model.InboundEventFacts;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Component;

import java.util.List;

/** 外部协议解码与本域事实映射，不替代冻结业务事实校验。 */
@Component
public class InboundEventGatewayImpl implements InboundEventGateway {
    @Override
    public InboundEventFacts.Paid paidFacts(String payload) {
        var event = parse(payload, OrderPaidEvent.class, "usePoint", "givePoint");
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
        var event = parse(payload, OrderClosedEvent.class);
        return new InboundEventFacts.Closed(event.orderNo(), event.memberId(), lines(event.items()));
    }

    @Override
    public InboundEventFacts.AfterSaleCompleted afterSaleFacts(String payload) {
        var event = parse(
            payload,
            AfterSaleCompletedEvent.class,
            "quantity", "returnUsePoint", "deductGivePoint", "ruleVersion", "fullyRefunded");
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
    public InboundEventFacts.Completed completedFacts(String payload) {
        var event = parse(payload, OrderCompletedEvent.class);
        return new InboundEventFacts.Completed(event.orderNo(), event.memberId());
    }

    @Override
    public InboundEventFacts.StockPreReserved stockPreReservedFacts(String payload) {
        var event = parse(payload, StockPreReservedEvent.class, "quantity");
        return new InboundEventFacts.StockPreReserved(
            event.reservationNo(), event.skuId(), event.quantity());
    }

    private <T> T parse(String payload, Class<T> type, String... primitiveFields) {
        var value = JsonUtils.toBean(payload, JSONObject.class);
        if (value == null) {
            throw new IllegalArgumentException("事件事实不能为空");
        }
        requirePrimitives(value, primitiveFields);
        var items = value.getJSONArray("items");
        if (items != null) {
            for (int index = 0; index < items.size(); index++) {
                requirePrimitives(items.getJSONObject(index), "quantity");
            }
        }
        return value.toJavaObject(type);
    }

    private void requirePrimitives(JSONObject value, String... fields) {
        if (value == null) {
            throw new IllegalArgumentException("库存行不能为空");
        }
        // 基本类型缺失会被JSON解析器默认为0/false，不能伪造冻结事实。
        for (String field : fields) {
            if (value.get(field) == null) {
                throw new IllegalArgumentException("事件事实缺少字段：" + field);
            }
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
