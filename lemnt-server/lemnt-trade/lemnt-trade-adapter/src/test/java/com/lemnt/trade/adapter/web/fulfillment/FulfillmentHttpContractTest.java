package com.lemnt.trade.adapter.web.fulfillment;

import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.trade.adapter.web.fulfillment.FulfillmentOrderController;
import com.lemnt.trade.adapter.web.fulfillment.ShipmentController;
import com.lemnt.trade.application.fulfillment.dto.OrderPageDTO;
import com.lemnt.trade.application.fulfillment.dto.OrderSummaryDTO;
import com.lemnt.trade.application.fulfillment.executor.OrderDetailQueryExe;
import com.lemnt.trade.application.fulfillment.executor.OrderPageQueryExe;
import com.lemnt.trade.application.fulfillment.executor.ShipmentCreateCmdExe;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FulfillmentHttpContractTest {
    JacksonJsonHttpMessageConverter converter() {
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        return new JacksonJsonHttpMessageConverter(builder.build());
    }

    @Test
    void missingShippingDataAndInvalidPagingRejectBeforeExecutor() throws Exception {
        var page = mock(OrderPageQueryExe.class);
        var ship = mock(ShipmentCreateCmdExe.class);
        var controller =
        new Object[] {
                new FulfillmentOrderController(page, mock(OrderDetailQueryExe.class)),
                new ShipmentController(ship)
        };
        var mvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(converter())
            .build();
        mvc.perform(
                post("/trade/admin/orders/TR1/shipment")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"requestNo\":\"S1\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/trade/admin/orders").param("pageSize", "101"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(page, ship);
    }



    @Test
    void orderListHasNoRecipientFieldsAndAllHttpActionsDeclarePermissions() throws Exception {
        var page = mock(OrderPageQueryExe.class);
        when(page.execute(any()))
            .thenReturn(
                new OrderPageDTO(
                List.of(
                new OrderSummaryDTO(
                "TR1",
                1L,
                new BigDecimal("17.00"),
                "SHIPPED",
                LocalDateTime.now(),
                true,
                false,
                "SF",
                "T1",
                LocalDateTime.now(),
                null)),
                1));
        var controller =
        new Object[] {
                new FulfillmentOrderController(page, mock(OrderDetailQueryExe.class)),
                new ShipmentController(mock(ShipmentCreateCmdExe.class))
        };
        var mvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setMessageConverters(converter())
            .build();
        mvc.perform(get("/trade/admin/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows[0].totalAmount").value("17.00"))
            .andExpect(jsonPath("$.rows[0].recipientName").doesNotExist())
            .andExpect(jsonPath("$.rows[0].phone").doesNotExist())
            .andExpect(jsonPath("$.rows[0].address").doesNotExist());
        for (var type :
                List.of(FulfillmentOrderController.class, ShipmentController.class)) {
            for (var method : type.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    assertNotNull(
                            method.getAnnotation(RequiresPermissions.class), method.toString());
                }
            }
        }
    }
}
