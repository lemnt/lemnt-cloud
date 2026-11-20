package com.lemnt.pay.domain.event.gateway;

import com.lemnt.pay.domain.event.model.InboundEventFacts;

/** 关单入站协议解析端口，不向应用层暴露订单Client。 */
public interface InboundEventGateway {
    /** 解析原订单关单事实。 */
    InboundEventFacts.Closed closedFacts(String payload);
}
