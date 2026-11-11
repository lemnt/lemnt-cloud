package com.lemnt.order.infrastructure.event.gateway;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.domain.event.gateway.InboundEventGateway;
import com.lemnt.order.domain.event.model.InboundEventFacts;
import com.lemnt.pay.client.event.PayRefundSuccessEvent;
import com.lemnt.pay.client.event.PaySuccessEvent;

import org.springframework.stereotype.Component;

/** 外部支付事件转为订单域事实，业务一致性仍由原消费事务校验。 */
@Component
public class InboundEventGatewayImpl implements InboundEventGateway {
    @Override
    public InboundEventFacts.PaymentSucceeded paymentSucceededFacts(String payload) {
        var event = JsonUtils.toBean(payload, PaySuccessEvent.class);
        if (event == null) {
            throw new IllegalArgumentException("支付事实不能为空");
        }
        return new InboundEventFacts.PaymentSucceeded(
            event.paymentNo(),
            event.orderNo(),
            event.memberId(),
            event.channel(),
            event.amount(),
            event.channelTradeNo());
    }

    @Override
    public InboundEventFacts.RefundSucceeded refundSucceededFacts(String payload) {
        var event = JsonUtils.toBean(payload, PayRefundSuccessEvent.class);
        if (event == null) {
            throw new IllegalArgumentException("退款事实不能为空");
        }
        return new InboundEventFacts.RefundSucceeded(
            event.refundNo(),
            event.afterSaleNo(),
            event.paymentNo(),
            event.orderNo(),
            event.memberId(),
            event.channel(),
            event.amount(),
            event.channelRefundNo());
    }
}
