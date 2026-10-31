package com.lemnt.trade.adapter.web.shipment;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.application.fulfillment.dto.ShipmentSummaryDTO;
import com.lemnt.trade.application.fulfillment.executor.ShipmentDetailQueryExe;
import com.lemnt.trade.application.fulfillment.executor.ShipmentPageQueryExe;
import com.lemnt.trade.application.fulfillment.executor.ShipmentTrackListQueryExe;
import com.lemnt.trade.application.fulfillment.qry.ShipmentPageQry;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ShipmentHttpTest {
    @Test
    void shipmentListHasIndependentPaginationAndNeverIncludesRecipientData() throws Exception {
        var query = mock(ShipmentPageQueryExe.class);
        when(query.execute(any())).thenReturn(PageResult.of(List.of(new ShipmentSummaryDTO(
                "TR1", "SF", "WAY1", LocalDateTime.now(), null)), 1));
        var mvc = MockMvcBuilders.standaloneSetup(new ShipmentQueryController(
                query, mock(ShipmentDetailQueryExe.class), mock(ShipmentTrackListQueryExe.class))).build();
        mvc.perform(get("/trade/admin/shipments").param("trackingNo", "WAY1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.rows[0].trackingNo").value("WAY1"))
                .andExpect(jsonPath("$.rows[0].recipientName").doesNotExist())
                .andExpect(jsonPath("$.rows[0].phone").doesNotExist());
        verify(query).execute(new ShipmentPageQry(null, null, "WAY1", null, 1, 20));
        mvc.perform(get("/trade/admin/shipments").param("pageSize", "101"))
                .andExpect(status().isBadRequest());
        verifyNoMoreInteractions(query);
    }
}
