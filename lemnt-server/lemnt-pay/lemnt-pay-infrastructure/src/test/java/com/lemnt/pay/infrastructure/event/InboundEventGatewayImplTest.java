package com.lemnt.pay.infrastructure.event;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.client.dto.OrderStockLineDTO;
import com.lemnt.order.client.event.OrderClosedEvent;
import com.lemnt.pay.domain.event.model.InboundEventFacts;
import com.lemnt.pay.infrastructure.event.gateway.InboundEventGatewayImpl;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 关单协议映射保留库存行、空集合语义与长ID，不重算原订单资源。 */
class InboundEventGatewayImplTest {
    private final InboundEventGatewayImpl gateway = new InboundEventGatewayImpl();

    @Test
    void closedFacts_preservesAllFieldsAndOriginalLineOrder() {
        var event = new OrderClosedEvent(
            "ORDER-1", Long.MAX_VALUE,
            List.of(new OrderStockLineDTO(Long.MAX_VALUE, 3), new OrderStockLineDTO(1L, 2)));
        assertEquals(
            new InboundEventFacts.Closed(
                "ORDER-1", Long.MAX_VALUE,
                List.of(new InboundEventFacts.StockLine(Long.MAX_VALUE, 3),
                    new InboundEventFacts.StockLine(1L, 2))),
            gateway.closedFacts(JsonUtils.toJsonStr(event)));
    }

    @Test
    void nullAndEmptyItemsRemainDistinct() {
        assertNull(gateway.closedFacts("{\"orderNo\":\"ORDER-1\",\"memberId\":7}").items());
        assertNull(gateway.closedFacts("{\"orderNo\":\"ORDER-1\",\"items\":null}").items());
        assertEquals(List.of(), gateway.closedFacts("{\"orderNo\":\"ORDER-1\",\"items\":[]}").items());
    }

    @Test
    void missingOrNullPrimitiveQuantityCannotSilentlyBecomeZero() {
        for (String payload : new String[]{
            "{\"items\":[{\"skuId\":1}]}",
            "{\"items\":[{\"skuId\":1,\"quantity\":null}]}",
            "{\"items\":[null]}",
            "{\"items\":[{\"skuId\":1,\"quantity\":\"bad\"}]}"}) {
            assertThrows(RuntimeException.class, () -> gateway.closedFacts(payload));
        }
    }

    @Test
    void mappingDoesNotInventMembershipOrApplyUnusedInventoryBusinessRules() {
        var value = gateway.closedFacts("{\"items\":[{\"quantity\":-2}]}");
        assertNull(value.orderNo());
        assertNull(value.memberId());
        assertEquals(List.of(new InboundEventFacts.StockLine(null, -2)), value.items());
    }

    @Test
    void emptyMalformedAndNullPayloadsCannotBecomeFacts() {
        for (String payload : new String[]{null, "", " ", "null", "[]", "{"}) {
            assertThrows(RuntimeException.class, () -> gateway.closedFacts(payload));
        }
    }
}
