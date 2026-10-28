package com.lemnt.trade.adapter.web.aftersale;

import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.trade.application.aftersale.cmd.AfterSaleCreateCmd;
import com.lemnt.trade.application.aftersale.dto.AfterSaleDTO;
import com.lemnt.trade.application.aftersale.executor.AfterSaleAgreeCmdExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleCreateCmdExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleDetailQueryExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleItemListQueryExe;
import com.lemnt.trade.application.aftersale.executor.AfterSalePageQueryExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleReceiveCmdExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleRefuseCmdExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleRejectCmdExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleReturnCmdExe;
import com.lemnt.trade.application.paymentrefund.executor.PaymentRefundCreateCmdExe;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AfterSaleHttpTest {
    @org.junit.jupiter.api.BeforeEach
    void user() { com.lemnt.common.security.context.SecurityContextHolder.setUserId("55"); }
    @org.junit.jupiter.api.AfterEach
    void clear() { com.lemnt.common.security.context.SecurityContextHolder.remove(); }
    AfterSaleCreateCmdExe create = mock(AfterSaleCreateCmdExe.class);

    AfterSaleController controller() {
        return new AfterSaleController(create, mock(AfterSaleAgreeCmdExe.class), mock(AfterSaleRejectCmdExe.class),
                mock(AfterSaleReturnCmdExe.class), mock(AfterSaleReceiveCmdExe.class), mock(AfterSaleRefuseCmdExe.class),
                mock(AfterSalePageQueryExe.class), mock(AfterSaleDetailQueryExe.class),
                mock(AfterSaleItemListQueryExe.class), mock(PaymentRefundCreateCmdExe.class));
    }

    JacksonJsonHttpMessageConverter converter() {
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        return new JacksonJsonHttpMessageConverter(builder.build());
    }

    @Test
    void itemAmountAndOrderIdentityAreDeterminedByApplicationAndPath() throws Exception {
        when(create.executeForAdmin(any(), eq(55L))).thenReturn(new AfterSaleDTO("RF1", "TR1", "KEY", 1L, "PAY1", 4L, 1,
                "REFUND_ONLY", new BigDecimal("8.50"), "申请原因", "REQUESTED", null, null, null,
                LocalDateTime.now(), null, null, null));
        var mvc = MockMvcBuilders.standaloneSetup(controller()).setMessageConverters(converter()).build();
        mvc.perform(post("/trade/admin/orders/TR1/after-sales").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"requestNo":"KEY","orderItemId":4,"quantity":1,"type":"REFUND_ONLY",
                                 "reason":"申请原因","amount":999,"orderNo":"OTHER"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderNo").value("TR1"))
                .andExpect(jsonPath("$.data.orderItemId").value("4"))
                .andExpect(jsonPath("$.data.amount").value("8.50"));
        verify(create).executeForAdmin(eq(new AfterSaleCreateCmd("TR1", "KEY", 4L, 1, "REFUND_ONLY", "申请原因")), eq(55L));
    }

    @Test
    void invalidQuantityOrTypeAndMissingReturnWaybillRejectBeforeWrite() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(controller()).setMessageConverters(converter()).build();
        mvc.perform(post("/trade/admin/orders/TR1/after-sales").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"requestNo":"KEY","orderItemId":4,"quantity":0,"type":"REFUND_ONLY","reason":"原因"}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/trade/admin/orders/TR1/after-sales").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"requestNo":"KEY","orderItemId":4,"quantity":1,"type":"OTHER","reason":"原因"}
                                """))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/trade/admin/after-sales/RF1/return").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(create);
    }
}
