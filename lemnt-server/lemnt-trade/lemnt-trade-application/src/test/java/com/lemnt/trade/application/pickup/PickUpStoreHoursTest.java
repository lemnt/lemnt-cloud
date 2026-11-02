package com.lemnt.trade.application.pickup;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.pickup.cmd.PickUpStoreSaveCmd;
import com.lemnt.trade.application.pickup.service.PickUpStoreService;
import com.lemnt.trade.domain.delivery.gateway.PickUpStoreGateway;
import com.lemnt.trade.domain.delivery.model.PickUpStore;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 门店维护应允许跨夜和全天营业，同时拒绝不存在的时刻。 */
class PickUpStoreHoursTest {
    final PickUpStoreGateway stores = mock(PickUpStoreGateway.class);
    final PickUpStoreService service = new PickUpStoreService(stores);

    PickUpStoreSaveCmd cmd(Long id, String open, String close) {
        return new PickUpStoreSaveCmd(id, "夜间店", null, "13800000000", 1L, "地址", null,
                open, close, null, null, List.of(), 0);
    }

    @Test
    void createsOvernightAndUpdatesToAllDay() {
        var saved = new PickUpStore(9L, "夜间店", null, "13800000000", 1L, "地址", null,
                "22:00:00", "06:00:00", null, null, List.of(), 0);
        when(stores.save(any())).thenReturn(saved);
        when(stores.find(9L)).thenReturn(saved);
        assertEquals(9L, service.create(cmd(null, "22:00:00", "06:00:00")));
        assertTrue(service.update(cmd(9L, "00:00:00", "00:00:00")));
        verify(stores, times(2)).save(any());
    }

    @Test
    void rejectsInvalidClockTimesAndFormats() {
        for (String invalid : List.of("99:00:00", "12:60:00", "24:00:00", "9:00:00", "12:00")) {
            assertThrows(ServiceException.class, () -> service.create(cmd(null, invalid, "06:00:00")));
            assertThrows(ServiceException.class, () -> service.create(cmd(null, "22:00:00", invalid)));
        }
        verifyNoInteractions(stores);
    }
}
