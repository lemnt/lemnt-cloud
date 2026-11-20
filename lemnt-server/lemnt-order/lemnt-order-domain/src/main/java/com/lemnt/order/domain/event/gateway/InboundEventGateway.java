package com.lemnt.order.domain.event.gateway;

import com.lemnt.order.domain.event.model.InboundEventFacts;

/** 支付入站协议解析端口，外部Client类型仅由基础设施实现消费。 */
public interface InboundEventGateway {
    /** 解析支付成功事实。 */
    InboundEventFacts.PaymentSucceeded paymentSucceededFacts(String payload);

    /** 解析退款成功事实。 */
    InboundEventFacts.RefundSucceeded refundSucceededFacts(String payload);
}
