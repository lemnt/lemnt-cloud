package com.lemnt.trade.infrastructure.order;

import com.lemnt.trade.domain.order.model.TradeOrderItem;
import com.lemnt.trade.infrastructure.order.converter.OrderItemConverter;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** 持久化边界不丢快照，也不把未知历史伪装成无规格。 */
class OrderProductSnapshotTest {
    @Test
    void snapshotRoundTripsAndDefensivelyCopiesProperties() {
        var properties = new java.util.ArrayList<>(List.of(new TradeOrderItem.Property(3L, "颜色", 7L, "黑色")));
        var item = item(properties);
        properties.clear();
        assertEquals("黑色", item.properties().get(0).valueName());
        var row = OrderItemConverter.toDO(9L, item);
        assertNull(row.getId());
        assertFalse(row.getCommentStatus());
        row.setId(77L);
        assertEquals(new TradeOrderItem(item.productId(), item.productCode(), item.productName(), item.unitPrice(),
                item.quantity(), item.lineAmount(), item.payAmount(), item.deliveryPrice(), item.spuId(), item.spuName(),
                item.properties(), item.picUrl(), 77L, false), OrderItemConverter.toModel(row));
        assertThrows(UnsupportedOperationException.class, () -> item.properties().clear());
    }

    @Test
    void unknownAndKnownEmptySpecsRemainDifferentAfterPersistence() {
        assertNull(OrderItemConverter.toModel(OrderItemConverter.toDO(9L, item(null))).properties());
        assertEquals(List.of(), OrderItemConverter.toModel(OrderItemConverter.toDO(9L, item(List.of()))).properties());
    }

    private TradeOrderItem item(List<TradeOrderItem.Property> properties) {
        return new TradeOrderItem(1L, "SKU", "商品", BigDecimal.TEN, 1, BigDecimal.TEN,
                BigDecimal.TEN, BigDecimal.ZERO, 2L, "SPU商品", properties, "https://example.com/a.png");
    }

    @Test
    void oldJsonObjectsPreserveNamesWithoutInventingIdentifiers() {
        var values = OrderItemConverter.properties("{\"颜色\":\"黑色\"}");
        assertEquals(List.of(new TradeOrderItem.Property(null, "颜色", null, "黑色")), values);
        assertEquals(List.of(), OrderItemConverter.properties("{}"));
        assertNull(OrderItemConverter.properties(null));
    }
}
