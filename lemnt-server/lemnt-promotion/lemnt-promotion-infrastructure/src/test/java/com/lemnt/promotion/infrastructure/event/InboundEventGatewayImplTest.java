package com.lemnt.promotion.infrastructure.event;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.dto.OrderStockLineDTO;
import com.lemnt.order.client.event.AfterSaleCompletedEvent;
import com.lemnt.order.client.event.OrderClosedEvent;
import com.lemnt.order.client.event.OrderCompletedEvent;
import com.lemnt.order.client.event.OrderPaidEvent;
import com.lemnt.product.client.event.StockPreReservedEvent;
import com.lemnt.promotion.domain.event.model.InboundEventFacts;
import com.lemnt.promotion.infrastructure.event.gateway.InboundEventGatewayImpl;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** 验证真实Client载荷到营销本域事实的无损映射。 */
class InboundEventGatewayImplTest {
    private final InboundEventGatewayImpl gateway = new InboundEventGatewayImpl();
    private final long memberId = Long.MAX_VALUE;
    private final long skuId = Long.MAX_VALUE - 1;

    @Test
    void paidFacts_preserveEveryFieldAndLargeNumbers() {
        var amount = new BigDecimal("12345678901234567.89");
        var coupons = Map.of(Long.MAX_VALUE - 2, 3);
        var payload = JsonUtils.toJsonStr(
            new OrderPaidEvent(
                "ORDER", memberId, amount, Long.MAX_VALUE - 3, Long.MAX_VALUE - 4,
                coupons, List.of(new OrderStockLineDTO(skuId, 7))));
        var expected = new InboundEventFacts.Paid(
            "ORDER", memberId, amount, Long.MAX_VALUE - 3, Long.MAX_VALUE - 4,
            coupons, List.of(new InboundEventFacts.StockLine(skuId, 7)));
        assertEquals(expected, gateway.paidFacts(payload));
        assertEquals(expected, gateway.paidFacts(payload));
    }

    @Test
    void closedCompletedAndReservedFacts_preserveEveryField() {
        var closed = gateway.closedFacts(JsonUtils.toJsonStr(
            new OrderClosedEvent("ORDER", memberId, List.of(new OrderStockLineDTO(skuId, 2)))));
        assertEquals(
            new InboundEventFacts.Closed(
                "ORDER", memberId, List.of(new InboundEventFacts.StockLine(skuId, 2))),
            closed);
        assertEquals(
            new InboundEventFacts.Completed("ORDER", memberId),
            gateway.completedFacts(JsonUtils.toJsonStr(new OrderCompletedEvent("ORDER", memberId))));
        assertEquals(
            new InboundEventFacts.StockPreReserved("RESERVATION", skuId, 4),
            gateway.stockPreReservedFacts(JsonUtils.toJsonStr(
                new StockPreReservedEvent("RESERVATION", skuId, 4))));
    }

    @Test
    void afterSaleFacts_preserveFrozenAmountsPointsAndIdentifiers() {
        var amount = new BigDecimal("9007199254740993.01");
        var event = new AfterSaleCompletedEvent(
            "ORDER", "AFTER_SALE", memberId, Long.MAX_VALUE - 2, skuId, 5,
            amount, Long.MAX_VALUE - 3, Long.MAX_VALUE - 4, 1, true);
        assertEquals(
            new InboundEventFacts.AfterSaleCompleted(
                "ORDER", "AFTER_SALE", memberId, Long.MAX_VALUE - 2, skuId, 5,
                amount, Long.MAX_VALUE - 3, Long.MAX_VALUE - 4, 1, true),
            gateway.afterSaleFacts(JsonUtils.toJsonStr(event)));
    }

    @Test
    void nullableCollections_remainNullRatherThanInventingEmptyFacts() {
        var paid = gateway.paidFacts(JsonUtils.toJsonStr(
            new OrderPaidEvent("ORDER", memberId, BigDecimal.ZERO, 0, 0, null, null)));
        assertNull(paid.giveCouponTemplateCounts());
        assertNull(paid.items());
        var closed = gateway.closedFacts(JsonUtils.toJsonStr(new OrderClosedEvent("ORDER", memberId, null)));
        assertNull(closed.items());
    }

    @Test
    void malformedOrEmptyPayloads_areRejectedForEveryEventType() {
        for (String payload : List.of("", "null", "[]", "{broken")) {
            assertThrows(RuntimeException.class, () -> gateway.paidFacts(payload));
            assertThrows(RuntimeException.class, () -> gateway.closedFacts(payload));
            assertThrows(RuntimeException.class, () -> gateway.afterSaleFacts(payload));
            assertThrows(RuntimeException.class, () -> gateway.completedFacts(payload));
            assertThrows(RuntimeException.class, () -> gateway.stockPreReservedFacts(payload));
        }
        assertThrows(IllegalArgumentException.class, () -> gateway.paidFacts(null));
    }

    @Test
    void missingOrNullPaidPrimitiveFields_areNotDefaultedToZero() {
        for (String field : List.of("usePoint", "givePoint")) {
            var value = paidPayload();
            value.remove(field);
            assertThrows(IllegalArgumentException.class, () -> gateway.paidFacts(value.toJSONString()));
        }
        assertThrows(IllegalArgumentException.class, () -> gateway.paidFacts(
            "{\"orderNo\":\"ORDER\",\"usePoint\":null,\"givePoint\":0}"));
    }

    @Test
    void missingOrNullAfterSalePrimitiveFields_areNotDefaultedToZeroOrFalse() {
        for (String field : List.of(
            "quantity", "returnUsePoint", "deductGivePoint", "ruleVersion", "fullyRefunded")) {
            var value = JsonUtils.toBean(JsonUtils.toJsonStr(new AfterSaleCompletedEvent(
                "ORDER", "AFTER_SALE", memberId, 3L, skuId, 2,
                BigDecimal.TEN, 4, 5, 1, false)), JSONObject.class);
            value.remove(field);
            assertThrows(IllegalArgumentException.class, () -> gateway.afterSaleFacts(value.toJSONString()));
        }
        assertThrows(IllegalArgumentException.class, () -> gateway.afterSaleFacts(
            "{\"quantity\":1,\"returnUsePoint\":0,\"deductGivePoint\":0,\"ruleVersion\":1,\"fullyRefunded\":null}"));
    }

    @Test
    void missingQuantityOrNullStockRows_areRejectedInsteadOfInventingZero() {
        assertThrows(IllegalArgumentException.class, () -> gateway.stockPreReservedFacts(
            "{\"reservationNo\":\"ORDER\",\"skuId\":1}"));
        assertThrows(IllegalArgumentException.class, () -> gateway.stockPreReservedFacts(
            "{\"reservationNo\":\"ORDER\",\"skuId\":1,\"quantity\":null}"));
        for (String items : List.of("[{\"skuId\":1}]", "[{\"skuId\":1,\"quantity\":null}]", "[null]")) {
            assertThrows(IllegalArgumentException.class, () -> gateway.closedFacts(
                "{\"orderNo\":\"ORDER\",\"items\":" + items + "}"));
            assertThrows(IllegalArgumentException.class, () -> gateway.paidFacts(
                "{\"orderNo\":\"ORDER\",\"usePoint\":0,\"givePoint\":0,\"items\":" + items + "}"));
        }
    }

    @Test
    void invalidBusinessValues_arePreservedForExistingHandlerValidation() {
        assertEquals(-1, gateway.stockPreReservedFacts(JsonUtils.toJsonStr(
            new StockPreReservedEvent("ORDER", skuId, -1))).quantity());
        var paid = gateway.paidFacts(JsonUtils.toJsonStr(new OrderPaidEvent(
            "ORDER", memberId, BigDecimal.valueOf(-1), -2, -3, Map.of(),
            List.of(new OrderStockLineDTO(skuId, 0)))));
        assertEquals(0, paid.items().get(0).quantity());
        assertEquals(-2, paid.usePoint());
        assertEquals(BigDecimal.valueOf(-1), paid.amount());
        var refunded = gateway.afterSaleFacts(JsonUtils.toJsonStr(new AfterSaleCompletedEvent(
            "ORDER", "AFTER_SALE", memberId, 3L, skuId, 0,
            BigDecimal.valueOf(-1), -2, -3, 9, false)));
        assertEquals(0, refunded.quantity());
        assertEquals(-2, refunded.returnUsePoint());
        assertEquals(-3, refunded.deductGivePoint());
        assertEquals(9, refunded.ruleVersion());
        assertFalse(refunded.fullyRefunded());
    }

    private JSONObject paidPayload() {
        return JsonUtils.toBean(JsonUtils.toJsonStr(new OrderPaidEvent(
            "ORDER", memberId, BigDecimal.TEN, 0, 0, Map.of(), List.of())), JSONObject.class);
    }
}
