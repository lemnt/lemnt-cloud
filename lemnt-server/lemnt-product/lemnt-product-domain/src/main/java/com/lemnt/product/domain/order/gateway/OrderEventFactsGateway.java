package com.lemnt.product.domain.order.gateway;

import com.lemnt.product.domain.order.model.AfterSaleCompletedFacts;
import com.lemnt.product.domain.order.model.OrderClosedFacts;
import com.lemnt.product.domain.order.model.OrderPaidFacts;

/**
 * 入站订单事件事实解析端口：外部契约（order-client事件）到商品域事实的转换隔离在基础设施层。
 * payload非法或事实为空时抛出，调用方不得按默认值降级处理。
 */
public interface OrderEventFactsGateway {
    /**
     * 解析支付事实。
     */
    OrderPaidFacts paidFacts(String payload);

    /**
     * 解析关单事实。
     */
    OrderClosedFacts closedFacts(String payload);

    /**
     * 解析售后完成事实。
     */
    AfterSaleCompletedFacts afterSaleFacts(String payload);
}
