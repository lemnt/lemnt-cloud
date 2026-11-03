package com.lemnt.order.infrastructure.order;

import com.lemnt.order.domain.order.model.TradeOrderItem;
import com.lemnt.order.infrastructure.order.converter.OrderItemConverter;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 持久化边界不丢快照，也不把未知历史伪装成无规格。 */
class OrderProductSnapshotTest {
    @Test
    void snapshotRoundTripsAndDefensivelyCopiesProperties() {
        var specs = new ArrayList<>(List.of(new TradeOrderItem.Spec(3L, "颜色", 7L, "黑色")));
        var item = item(specs);
        specs.clear();
        assertEquals("黑色", item.specs().get(0).specValueName());
        var row = OrderItemConverter.toDO(9L, item);
        assertNull(row.getId());
        assertFalse(row.getCommentStatus());
        row.setId(77L);
        assertEquals(
                new TradeOrderItem(
                        item.productId(),
                        item.productCode(),
                        item.productName(),
                        item.unitPrice(),
                        item.quantity(),
                        item.lineAmount(),
                        item.payAmount(),
                        item.deliveryPrice(),
                        item.spuId(),
                        item.spuName(),
                        item.specs(),
                        item.picUrl(),
                        77L,
                        false),
                OrderItemConverter.toModel(row));
        assertThrows(UnsupportedOperationException.class, () -> item.specs().clear());
    }

    @Test
    void unknownAndKnownEmptySpecsRemainDifferentAfterPersistence() {
        assertNull(OrderItemConverter.toModel(OrderItemConverter.toDO(9L, item(null))).specs());
        assertEquals(
                List.of(),
                OrderItemConverter.toModel(OrderItemConverter.toDO(9L, item(List.of()))).specs());
    }

    private TradeOrderItem item(List<TradeOrderItem.Spec> specs) {
        return new TradeOrderItem(
                1L,
                "SKU",
                "商品",
                BigDecimal.TEN,
                1,
                BigDecimal.TEN,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                2L,
                "SPU商品",
                specs,
                "https://example.com/a.png");
    }

    @Test
    void oldJsonObjectsPreserveNamesWithoutInventingIdentifiers() {
        var values = OrderItemConverter.specs("{\"颜色\":\"黑色\"}");
        assertEquals(List.of(new TradeOrderItem.Spec(null, "颜色", null, "黑色")), values);
        assertEquals(List.of(), OrderItemConverter.specs("{}"));
        assertNull(OrderItemConverter.specs(null));
    }

    /** 旧四字段JSON保留实际编号，新写仅输出spec命名。 */
    @Test
    void legacyFourFieldsBecomeSpecsWithoutLosingIdentifiers() {
        var specs =
                OrderItemConverter.specs(
                        "[{\"propertyId\":3,\"propertyName\":\"颜色\",\"valueId\":7,\"valueName\":\"黑色\"}]");
        assertEquals(List.of(new TradeOrderItem.Spec(3L, "颜色", 7L, "黑色")), specs);
        var json = OrderItemConverter.toDO(9L, item(specs)).getSpecs();
        assertTrue(json.contains("specValueId"));
        assertFalse(json.contains("propertyId"));
        assertFalse(json.contains("valueName"));
    }
}
