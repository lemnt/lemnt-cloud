package com.lemnt.trade.infrastructure.order;

import com.lemnt.trade.domain.order.model.TradeOrderItem;
import com.lemnt.trade.infrastructure.order.converter.OrderItemConverter;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** 持久化边界不丢快照，也不把未知历史伪装成无规格。 */
class OrderProductSnapshotTest {
    @Test
    void snapshotRoundTripsAndDefensivelyCopiesProperties() {
        var properties = new java.util.HashMap<>(Map.of("颜色", "黑色"));
        var item = item(properties);
        properties.put("颜色", "白色");
        assertEquals("黑色", item.properties().get("颜色"));
        assertEquals(item, OrderItemConverter.toModel(OrderItemConverter.toDO(9L, item)));
        assertThrows(UnsupportedOperationException.class, () -> item.properties().put("尺码", "L"));
    }

    @Test
    void unknownAndKnownEmptySpecsRemainDifferentAfterPersistence() {
        assertNull(OrderItemConverter.toModel(OrderItemConverter.toDO(9L, item(null))).properties());
        assertEquals(Map.of(), OrderItemConverter.toModel(OrderItemConverter.toDO(9L, item(Map.of()))).properties());
    }

    private TradeOrderItem item(Map<String, String> properties) {
        return new TradeOrderItem(1L, "SKU", "商品", BigDecimal.TEN, 1, BigDecimal.TEN,
                BigDecimal.TEN, BigDecimal.ZERO, 2L, "SPU商品", properties, "https://example.com/a.png");
    }
}
