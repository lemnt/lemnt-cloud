package com.lemnt.member.infrastructure.event;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.member.infrastructure.event.gateway.InboundEventGatewayImpl;
import com.lemnt.order.client.dto.OrderStockLineDTO;
import com.lemnt.order.client.event.AfterSaleCompletedEvent;
import com.lemnt.order.client.event.OrderClosedEvent;
import com.lemnt.order.client.event.OrderPaidEvent;
import com.lemnt.order.client.event.OrderShippedEvent;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InboundEventGatewayImplTest {
    private final InboundEventGatewayImpl gateway = new InboundEventGatewayImpl();
    private static final Long MEMBER_ID = Long.MAX_VALUE;
    private static final Long SKU_ID = Long.MAX_VALUE - 1;
    private static final BigDecimal AMOUNT = new BigDecimal("12345678901234567.89");
    private static final long USE_POINT = 9007199254740993L;
    private static final long GIVE_POINT = Long.MAX_VALUE;

    @Test
    void paidFacts_preserveEveryFieldAndExactNumbers() {
        var event = paidEvent();
        var value = gateway.paidFacts(JsonUtils.toJsonStr(event));
        assertEquals(event.orderNo(), value.orderNo());
        assertEquals(MEMBER_ID, value.memberId());
        assertEquals(AMOUNT, value.amount());
        assertEquals(USE_POINT, value.usePoint());
        assertEquals(GIVE_POINT, value.givePoint());
        assertEquals(event.giveCouponTemplateCounts(), value.giveCouponTemplateCounts());
        assertEquals(1, value.items().size());
        assertEquals(SKU_ID, value.items().get(0).skuId());
        assertEquals(3, value.items().get(0).quantity());
    }

    @Test
    void closedAndShippedFacts_preserveIdentifiersAndStockLines() {
        var closed = gateway.closedFacts(JsonUtils.toJsonStr(
            new OrderClosedEvent("O1", MEMBER_ID, List.of(new OrderStockLineDTO(SKU_ID, 3)))));
        assertEquals("O1", closed.orderNo());
        assertEquals(MEMBER_ID, closed.memberId());
        assertEquals(SKU_ID, closed.items().get(0).skuId());
        assertEquals(3, closed.items().get(0).quantity());
        var shipped = gateway.shippedFacts(JsonUtils.toJsonStr(new OrderShippedEvent("O1", MEMBER_ID)));
        assertEquals("O1", shipped.orderNo());
        assertEquals(MEMBER_ID, shipped.memberId());
    }

    @Test
    void afterSaleFacts_preserveEveryFrozenField() {
        var event = afterSaleEvent();
        var value = gateway.afterSaleFacts(JsonUtils.toJsonStr(event));
        assertEquals(event.orderNo(), value.orderNo());
        assertEquals(event.afterSaleNo(), value.afterSaleNo());
        assertEquals(MEMBER_ID, value.memberId());
        assertEquals(event.orderItemId(), value.orderItemId());
        assertEquals(SKU_ID, value.skuId());
        assertEquals(3, value.quantity());
        assertEquals(AMOUNT, value.amount());
        assertEquals(USE_POINT, value.returnUsePoint());
        assertEquals(GIVE_POINT, value.deductGivePoint());
        assertEquals(1, value.ruleVersion());
        assertTrue(value.fullyRefunded());
    }

    @Test
    void nullCollections_remainUnknownInsteadOfBecomingEmpty() {
        var paid = gateway.paidFacts(JsonUtils.toJsonStr(
            new OrderPaidEvent("O1", MEMBER_ID, AMOUNT, 0, 0, null, null)));
        assertNull(paid.items());
        assertNull(paid.giveCouponTemplateCounts());
        assertNull(gateway.closedFacts(JsonUtils.toJsonStr(
            new OrderClosedEvent("O1", MEMBER_ID, null))).items());
        var empty = gateway.paidFacts(JsonUtils.toJsonStr(
            new OrderPaidEvent("O1", MEMBER_ID, AMOUNT, 0, 0, Map.of(), List.of())));
        assertTrue(empty.items().isEmpty());
        assertTrue(empty.giveCouponTemplateCounts().isEmpty());
    }

    @Test
    void missingOrNullPrimitiveFields_cannotBecomeZeroOrFalse() {
        for (String field : List.of("usePoint", "givePoint")) {
            var json = JsonUtils.toBean(JsonUtils.toJsonStr(paidEvent()), JSONObject.class);
            json.remove(field);
            assertThrows(IllegalArgumentException.class, () -> gateway.paidFacts(json.toJSONString()));
            json.put(field, null);
            assertThrows(IllegalArgumentException.class,
                () -> gateway.paidFacts(json.toJSONString(JSONWriter.Feature.WriteNulls)));
        }
        for (String field : List.of(
            "quantity", "returnUsePoint", "deductGivePoint", "ruleVersion", "fullyRefunded")) {
            var json = JsonUtils.toBean(JsonUtils.toJsonStr(afterSaleEvent()), JSONObject.class);
            json.remove(field);
            assertThrows(IllegalArgumentException.class, () -> gateway.afterSaleFacts(json.toJSONString()));
            json.put(field, null);
            assertThrows(IllegalArgumentException.class,
                () -> gateway.afterSaleFacts(json.toJSONString(JSONWriter.Feature.WriteNulls)));
        }
        var paid = JsonUtils.toBean(JsonUtils.toJsonStr(paidEvent()), JSONObject.class);
        paid.getJSONArray("items").getJSONObject(0).remove("quantity");
        assertThrows(IllegalArgumentException.class, () -> gateway.paidFacts(paid.toJSONString()));
        var closed = JsonUtils.toBean(JsonUtils.toJsonStr(
            new OrderClosedEvent("O1", MEMBER_ID, List.of(new OrderStockLineDTO(SKU_ID, 3)))), JSONObject.class);
        closed.getJSONArray("items").getJSONObject(0).put("quantity", null);
        assertThrows(IllegalArgumentException.class,
            () -> gateway.closedFacts(closed.toJSONString(JSONWriter.Feature.WriteNulls)));
    }

    @Test
    void emptyOrMalformedPayloads_failInsteadOfCreatingFacts() {
        for (String payload : List.of("", "null")) {
            assertThrows(IllegalArgumentException.class, () -> gateway.paidFacts(payload));
            assertThrows(IllegalArgumentException.class, () -> gateway.closedFacts(payload));
            assertThrows(IllegalArgumentException.class, () -> gateway.afterSaleFacts(payload));
            assertThrows(IllegalArgumentException.class, () -> gateway.shippedFacts(payload));
        }
        assertThrows(IllegalArgumentException.class, () -> gateway.paidFacts("{}"));
        assertThrows(JSONException.class, () -> gateway.paidFacts("{"));
    }

    @Test
    void invalidBusinessValues_areNotRewrittenBeforeHandlerValidation() {
        var paid = gateway.paidFacts(JsonUtils.toJsonStr(
            new OrderPaidEvent("O1", MEMBER_ID, new BigDecimal("-1.00"), -2, -3,
                Map.of(SKU_ID, -1), List.of(new OrderStockLineDTO(SKU_ID, 0)))));
        assertEquals(new BigDecimal("-1.00"), paid.amount());
        assertEquals(-2, paid.usePoint());
        assertEquals(-3, paid.givePoint());
        assertEquals(-1, paid.giveCouponTemplateCounts().get(SKU_ID));
        assertEquals(0, paid.items().get(0).quantity());
        var refunded = gateway.afterSaleFacts(JsonUtils.toJsonStr(
            new AfterSaleCompletedEvent("O1", "A1", MEMBER_ID, 5L, SKU_ID,
                -1, new BigDecimal("-2.00"), -3, -4, 9, false)));
        assertEquals(-1, refunded.quantity());
        assertEquals(new BigDecimal("-2.00"), refunded.amount());
        assertEquals(-3, refunded.returnUsePoint());
        assertEquals(-4, refunded.deductGivePoint());
        assertEquals(9, refunded.ruleVersion());
        assertFalse(refunded.fullyRefunded());
    }

    private OrderPaidEvent paidEvent() {
        return new OrderPaidEvent("O1", MEMBER_ID, AMOUNT, USE_POINT, GIVE_POINT,
            Map.of(SKU_ID, 4), List.of(new OrderStockLineDTO(SKU_ID, 3)));
    }

    private AfterSaleCompletedEvent afterSaleEvent() {
        return new AfterSaleCompletedEvent("O1", "A1", MEMBER_ID, Long.MAX_VALUE - 2,
            SKU_ID, 3, AMOUNT, USE_POINT, GIVE_POINT, 1, true);
    }
}
