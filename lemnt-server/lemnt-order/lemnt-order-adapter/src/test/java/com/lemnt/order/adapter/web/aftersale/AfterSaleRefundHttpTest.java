package com.lemnt.order.adapter.web.aftersale;

import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.order.application.aftersale.executor.*;
import com.lemnt.order.application.paymentrefund.dto.PaymentRefundDTO;
import com.lemnt.order.application.paymentrefund.executor.PaymentRefundCreateCmdExe;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 从原支付管理组合用例迁入订单：保留退款入口归属及金额防伪断言。
 */
class AfterSaleRefundHttpTest {
    JacksonJsonHttpMessageConverter converter() {
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        return new JacksonJsonHttpMessageConverter(builder.build());
    }

    @AfterEach
    void clearIdentity() {
        SecurityContextHolder.remove();
    }

    @Test
    void refundSubmissionAcceptsOnlyAfterSaleIdentityAndReturnsServerTransaction()
        throws Exception {
        SecurityContextHolder.setUserId("55");
        var create = mock(PaymentRefundCreateCmdExe.class);
        when(create.executeForAdmin("RF1", 55L))
            .thenReturn(
                new PaymentRefundDTO(
                    "PR1",
                    "RF1",
                    "PAY1",
                    "TR1",
                    "ALIPAY",
                    new BigDecimal("17.00"),
                    "WAITING",
                    null));
        var mvc =
            MockMvcBuilders.standaloneSetup(
                    new AfterSaleController(
                        mock(AfterSaleCreateCmdExe.class),
                        mock(AfterSaleAgreeCmdExe.class),
                        mock(AfterSaleRejectCmdExe.class),
                        mock(AfterSaleReturnCmdExe.class),
                        mock(AfterSaleReceiveCmdExe.class),
                        mock(AfterSaleRefuseCmdExe.class),
                        mock(AfterSalePageQueryExe.class),
                        mock(AfterSaleDetailQueryExe.class),
                        mock(AfterSaleItemListQueryExe.class),
                        create))
                .setMessageConverters(converter())
                .build();
        mvc.perform(
                post("/trade/admin/after-sales/RF1/refund")
                    .param("amount", "999")
                    .param("channel", "OTHER")
                    .param("paymentNo", "PAY999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.afterSaleNo").value("RF1"))
            .andExpect(jsonPath("$.data.paymentNo").value("PAY1"))
            .andExpect(jsonPath("$.data.channel").value("ALIPAY"))
            .andExpect(jsonPath("$.data.amount").value("17.00"))
            .andExpect(jsonPath("$.data.status").value("WAITING"));
        verify(create).executeForAdmin("RF1", 55L);
    }
}
