package com.lemnt.promotion.domain.event.gateway;

import com.lemnt.promotion.domain.event.model.InboundEventFacts;

/** 外部载荷只由基础设施解析，业务校验仍由所属消费链完成。 */
public interface InboundEventGateway {
    /** 解析订单支付事实。 */
    InboundEventFacts.Paid paidFacts(String payload);

    /** 解析订单关闭事实。 */
    InboundEventFacts.Closed closedFacts(String payload);

    /** 解析售后完成事实。 */
    InboundEventFacts.AfterSaleCompleted afterSaleFacts(String payload);

    /** 解析订单完成事实。 */
    InboundEventFacts.Completed completedFacts(String payload);

    /** 解析库存预占事实。 */
    InboundEventFacts.StockPreReserved stockPreReservedFacts(String payload);
}
