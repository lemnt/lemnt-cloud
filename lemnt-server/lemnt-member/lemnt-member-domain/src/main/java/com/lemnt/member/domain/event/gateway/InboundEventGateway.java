package com.lemnt.member.domain.event.gateway;

import com.lemnt.member.domain.event.model.InboundEventFacts;

/** 入站协议解析在基础设施层完成，非法载荷不得降级为默认事实。 */
public interface InboundEventGateway {
    /** 解析订单支付事实。 */
    InboundEventFacts.Paid paidFacts(String payload);

    /** 解析订单关闭事实。 */
    InboundEventFacts.Closed closedFacts(String payload);

    /** 解析售后完成事实。 */
    InboundEventFacts.AfterSaleCompleted afterSaleFacts(String payload);

    /** 解析订单发货事实。 */
    InboundEventFacts.Shipped shippedFacts(String payload);
}
