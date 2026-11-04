package com.lemnt.order.application.pickup;

import com.lemnt.order.application.pickup.qry.AppPickUpStoreListQry;
import com.lemnt.order.application.pickup.service.PickUpStoreService;
import com.lemnt.order.domain.delivery.gateway.PickUpStoreGateway;
import com.lemnt.order.domain.delivery.model.PickUpStore;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 门店列表按当前位置直线距离排序，缺少坐标的门店排在最后。
 */
class PickUpStoreDistanceTest {
    final PickUpStoreGateway gateway = mock(PickUpStoreGateway.class);
    final PickUpStoreService service = new PickUpStoreService(gateway);

    PickUpStore store(long id, String lat, String lng) {
        return new PickUpStore(
            id,
            "门店" + id,
            null,
            "13800000000",
            1L,
            "地址",
            null,
            "00:00:00",
            "23:59:59",
            lat == null ? null : new BigDecimal(lat),
            lng == null ? null : new BigDecimal(lng),
            List.of(),
            0);
    }

    @Test
    void sortsByDistanceAndPutsMissingCoordinatesLast() {
        when(gateway.enabled())
            .thenReturn(
                List.of(
                    store(3L, null, null),
                    store(2L, "31.2400", "121.4900"),
                    store(1L, "31.2300", "121.4700")));

        var result =
            service.appList(
                new AppPickUpStoreListQry(
                    new BigDecimal("31.2300"), new BigDecimal("121.4700")));

        assertEquals(List.of(1L, 2L, 3L), result.stream().map(value -> value.id()).toList());
        assertEquals(new BigDecimal("0.00"), result.get(0).distanceKm());
        assertNull(result.get(2).distanceKm());
    }

    @Test
    void withoutLocationKeepsStoreOrder() {
        when(gateway.enabled())
            .thenReturn(List.of(store(3L, null, null), store(1L, "31.2300", "121.4700")));
        var result = service.appList(new AppPickUpStoreListQry(null, null));
        assertEquals(List.of(3L, 1L), result.stream().map(value -> value.id()).toList());
        assertTrue(result.stream().allMatch(value -> value.distanceKm() == null));
    }
}
