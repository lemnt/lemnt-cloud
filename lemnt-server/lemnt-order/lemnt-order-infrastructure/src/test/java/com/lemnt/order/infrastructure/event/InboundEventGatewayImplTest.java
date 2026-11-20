package com.lemnt.order.infrastructure.event;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.domain.event.model.InboundEventFacts;
import com.lemnt.order.infrastructure.event.gateway.InboundEventGatewayImpl;
import com.lemnt.pay.client.event.PayRefundSuccessEvent;
import com.lemnt.pay.client.event.PaySuccessEvent;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** 外部资金协议字段必须无损进入订单域，状态核验仍由原消费事务承担。 */
class InboundEventGatewayImplTest {
    private final InboundEventGatewayImpl gateway = new InboundEventGatewayImpl();

    @Test
    void paymentSucceededFacts_preservesAllFieldsAndExactAmounts() {
        var amount = new BigDecimal("99999999999999.99");
        var event = new PaySuccessEvent(
            "PAY-1", "ORDER-1", Long.MAX_VALUE, "ALIPAY", amount, "TRADE-1");
        assertEquals(
            new InboundEventFacts.PaymentSucceeded(
                "PAY-1", "ORDER-1", Long.MAX_VALUE, "ALIPAY", amount, "TRADE-1"),
            gateway.paymentSucceededFacts(JsonUtils.toJsonStr(event)));
    }

    @Test
    void refundSucceededFacts_preservesEveryFrozenIdentifierAndAmount() {
        var amount = new BigDecimal("3.32");
        var event = new PayRefundSuccessEvent(
            "REFUND-1", "AFTER-1", "PAY-1", "ORDER-1", Long.MAX_VALUE,
            "WECHAT", amount, "CHANNEL-REFUND-1");
        assertEquals(
            new InboundEventFacts.RefundSucceeded(
                "REFUND-1", "AFTER-1", "PAY-1", "ORDER-1", Long.MAX_VALUE,
                "WECHAT", amount, "CHANNEL-REFUND-1"),
            gateway.refundSucceededFacts(JsonUtils.toJsonStr(event)));
    }

    @Test
    void nullableFieldsAreNotInventedOrConvertedToDefaultValues() {
        var paid = gateway.paymentSucceededFacts("{\"paymentNo\":\"PAY-1\",\"orderNo\":\"ORDER-1\"}");
        assertNull(paid.memberId());
        assertNull(paid.amount());
        assertNull(paid.channel());
        assertNull(paid.channelTradeNo());
        var refund = gateway.refundSucceededFacts("{\"refundNo\":\"REFUND-1\"}");
        assertNull(refund.afterSaleNo());
        assertNull(refund.paymentNo());
        assertNull(refund.orderNo());
        assertNull(refund.memberId());
        assertNull(refund.channel());
        assertNull(refund.amount());
        assertNull(refund.channelRefundNo());
    }

    @Test
    void businessAmountRulesRemainWithTheExistingHandlers() {
        assertEquals(
            new BigDecimal("-1.00"),
            gateway.paymentSucceededFacts("{\"amount\":-1.00}").amount());
        assertEquals(
            new BigDecimal("0.00"),
            gateway.refundSucceededFacts("{\"amount\":0.00}").amount());
    }

    @Test
    void emptyMalformedAndNullPayloadsCannotBecomeFacts() {
        for (String payload : new String[]{null, "", " ", "null", "[]", "{", "{\"amount\":\"bad\"}"}) {
            assertThrows(RuntimeException.class, () -> gateway.paymentSucceededFacts(payload));
            assertThrows(RuntimeException.class, () -> gateway.refundSucceededFacts(payload));
        }
    }
}
