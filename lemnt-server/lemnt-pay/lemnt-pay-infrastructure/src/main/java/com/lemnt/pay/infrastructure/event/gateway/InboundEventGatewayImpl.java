package com.lemnt.pay.infrastructure.event.gateway;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.event.OrderClosedEvent;
import com.lemnt.pay.domain.event.gateway.InboundEventGateway;
import com.lemnt.pay.domain.event.model.InboundEventFacts;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Component;

/** 订单关单协议转为支付域事实，不在此决定支付状态。 */
@Component
public class InboundEventGatewayImpl implements InboundEventGateway {
    @Override
    public InboundEventFacts.Closed closedFacts(String payload) {
        var value = JsonUtils.toBean(payload, JSONObject.class);
        if (value == null) {
            throw new IllegalArgumentException("关单事实不能为空");
        }
        var items = value.getJSONArray("items");
        if (items != null) {
            for (int index = 0; index < items.size(); index++) {
                var item = items.getJSONObject(index);
                // primitive默认零会掩盖缺失件数，不能把未知事实变成真实零。
                if (item == null || item.get("quantity") == null) {
                    throw new IllegalArgumentException("库存行件数不能为空");
                }
            }
        }
        var event = JsonUtils.toBean(payload, OrderClosedEvent.class);
        return new InboundEventFacts.Closed(
            event.orderNo(),
            event.memberId(),
            event.items() == null
                ? null
                : event.items().stream()
                    .map(item -> new InboundEventFacts.StockLine(item.skuId(), item.quantity()))
                    .toList());
    }
}
