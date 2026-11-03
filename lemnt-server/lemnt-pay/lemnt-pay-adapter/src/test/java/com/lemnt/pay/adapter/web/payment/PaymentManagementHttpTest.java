package com.lemnt.pay.adapter.web.payment;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.pay.adapter.web.paymentrefund.PaymentRefundController;
import com.lemnt.pay.application.payment.dto.PaymentDTO;
import com.lemnt.pay.application.payment.executor.PaymentChannelListQueryExe;
import com.lemnt.pay.application.payment.executor.PaymentDetailQueryExe;
import com.lemnt.pay.application.payment.executor.PaymentPageQueryExe;
import com.lemnt.pay.application.payment.qry.PaymentPageQry;
import com.lemnt.pay.application.paymentrefund.dto.PaymentRefundDTO;
import com.lemnt.pay.application.paymentrefund.executor.PaymentRefundDetailQueryExe;
import com.lemnt.pay.application.paymentrefund.executor.PaymentRefundPageQueryExe;
import com.lemnt.pay.application.paymentrefund.qry.PaymentRefundPageQry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 后台支付管理 HTTP 契约。 */
class PaymentManagementHttpTest {
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
    void supportedChannelsContainOnlyCodeAndDisplayName() throws Exception {
        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new PaymentChannelController(new PaymentChannelListQueryExe()))
                        .setMessageConverters(converter())
                        .build();
        mvc.perform(get("/trade/admin/payment-channels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].code").value("ALIPAY"))
                .andExpect(jsonPath("$.data[0].name").value("支付宝"))
                .andExpect(jsonPath("$.data[1].code").value("WECHAT"))
                .andExpect(jsonPath("$.data[2].code").value("SANDBOX"))
                .andExpect(jsonPath("$.data[0].enabled").doesNotExist());
    }

    @Test
    void paymentPaginationUsesUnifiedRowsAndExactMoneyAndHidesInternalNotification()
            throws Exception {
        var query = mock(PaymentPageQueryExe.class);
        when(query.execute(any()))
                .thenReturn(
                        PageResult.of(
                                List.of(
                                        new PaymentDTO(
                                                1L,
                                                "PAY1",
                                                "TR1",
                                                "ALIPAY",
                                                "CHANNEL1",
                                                new BigDecimal("17.00"),
                                                "SUCCESS")),
                                1));
        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new PaymentController(query, mock(PaymentDetailQueryExe.class)))
                        .setMessageConverters(converter())
                        .build();
        mvc.perform(get("/trade/admin/payments").param("orderNo", "TR1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.rows[0].amount").value("17.00"))
                .andExpect(jsonPath("$.rows[0].paymentNo").value("PAY1"))
                .andExpect(jsonPath("$.rows[0].notifyId").doesNotExist());
        verify(query).execute(new PaymentPageQry(null, "TR1", null, null, 1, 20));
        mvc.perform(get("/trade/admin/payments").param("pageSize", "101"))
                .andExpect(status().isBadRequest());
        verifyNoMoreInteractions(query);
    }

    @Test
    void refundPaginationHasIndependentContractAndMissingDetailIsConcealed() throws Exception {
        var query = mock(PaymentRefundPageQueryExe.class);
        var detail = mock(PaymentRefundDetailQueryExe.class);
        when(query.execute(any()))
                .thenReturn(
                        PageResult.of(
                                List.of(
                                        new PaymentRefundDTO(
                                                "PR1",
                                                "RF1",
                                                "PAY1",
                                                "TR1",
                                                "WECHAT",
                                                new BigDecimal("17.00"),
                                                "SUCCESS",
                                                "CHANNEL-R1")),
                                1));
        var mvc =
                MockMvcBuilders.standaloneSetup(new PaymentRefundController(query, detail))
                        .setMessageConverters(converter())
                        .build();
        mvc.perform(get("/trade/admin/payment-refunds").param("afterSaleNo", "RF1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].refundNo").value("PR1"))
                .andExpect(jsonPath("$.rows[0].amount").value("17.00"));
        verify(query).execute(new PaymentRefundPageQry("RF1", null, null, 1, 20));
        mvc.perform(get("/trade/admin/payment-refunds/missing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("交易记录不存在"));
        mvc.perform(get("/trade/admin/payment-refunds").param("pageNum", "0"))
                .andExpect(status().isBadRequest());
        verifyNoMoreInteractions(query);
    }
}
