package com.lemnt.trade.adapter.web.payment;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.trade.adapter.web.aftersale.AfterSaleController;
import com.lemnt.trade.application.aftersale.executor.*;
import com.lemnt.trade.adapter.web.paymentrefund.PaymentRefundController;
import com.lemnt.trade.application.payment.dto.PaymentDTO;
import com.lemnt.trade.application.payment.executor.PaymentChannelListQueryExe;
import com.lemnt.trade.application.payment.executor.PaymentDetailQueryExe;
import com.lemnt.trade.application.payment.executor.PaymentPageQueryExe;
import com.lemnt.trade.application.payment.qry.PaymentPageQry;
import com.lemnt.trade.application.paymentrefund.dto.PaymentRefundDTO;
import com.lemnt.trade.application.paymentrefund.executor.PaymentRefundCreateCmdExe;
import com.lemnt.trade.application.paymentrefund.executor.PaymentRefundDetailQueryExe;
import com.lemnt.trade.application.paymentrefund.executor.PaymentRefundPageQueryExe;
import com.lemnt.trade.application.paymentrefund.qry.PaymentRefundPageQry;

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

    @org.junit.jupiter.api.AfterEach
    void clearIdentity() { com.lemnt.common.security.context.SecurityContextHolder.remove(); }

    @Test
    void supportedChannelsContainOnlyCodeAndDisplayName() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new PaymentChannelController(new PaymentChannelListQueryExe()))
                .setMessageConverters(converter()).build();
        mvc.perform(get("/trade/admin/payment-channels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].code").value("ALIPAY"))
                .andExpect(jsonPath("$.data[0].name").value("支付宝"))
                .andExpect(jsonPath("$.data[1].code").value("WECHAT"))
                .andExpect(jsonPath("$.data[0].enabled").doesNotExist());
    }

    @Test
    void paymentPaginationUsesUnifiedRowsAndExactMoneyAndHidesInternalNotification() throws Exception {
        var query = mock(PaymentPageQueryExe.class);
        when(query.execute(any())).thenReturn(PageResult.of(List.of(new PaymentDTO(
                1L, "PAY1", "TR1", "ALIPAY", "CHANNEL1", new BigDecimal("17.00"), "SUCCESS")), 1));
        var mvc = MockMvcBuilders.standaloneSetup(new PaymentController(query, mock(PaymentDetailQueryExe.class)))
                .setMessageConverters(converter()).build();
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
    void refundSubmissionAcceptsOnlyAfterSaleIdentityAndReturnsServerTransaction() throws Exception {
        com.lemnt.common.security.context.SecurityContextHolder.setUserId("55");
        var create = mock(PaymentRefundCreateCmdExe.class);
        when(create.executeForAdmin("RF1", 55L)).thenReturn(new PaymentRefundDTO(
                "PR1", "RF1", "PAY1", "TR1", "ALIPAY", new BigDecimal("17.00"), "WAITING", null));
        var mvc = MockMvcBuilders.standaloneSetup(new AfterSaleController(mock(AfterSaleCreateCmdExe.class), mock(AfterSaleAgreeCmdExe.class),
                mock(AfterSaleRejectCmdExe.class), mock(AfterSaleReturnCmdExe.class),
                mock(AfterSaleReceiveCmdExe.class), mock(AfterSaleRefuseCmdExe.class),
                mock(AfterSalePageQueryExe.class), mock(AfterSaleDetailQueryExe.class),
                mock(AfterSaleItemListQueryExe.class), create))
                .setMessageConverters(converter()).build();
        mvc.perform(post("/trade/admin/after-sales/RF1/refund")
                        .param("amount", "999").param("channel", "OTHER").param("paymentNo", "PAY999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.afterSaleNo").value("RF1"))
                .andExpect(jsonPath("$.data.paymentNo").value("PAY1"))
                .andExpect(jsonPath("$.data.channel").value("ALIPAY"))
                .andExpect(jsonPath("$.data.amount").value("17.00"))
                .andExpect(jsonPath("$.data.status").value("WAITING"));
        verify(create).executeForAdmin("RF1", 55L);
    }

    @Test
    void refundPaginationHasIndependentContractAndMissingDetailIsConcealed() throws Exception {
        var query = mock(PaymentRefundPageQueryExe.class);
        var detail = mock(PaymentRefundDetailQueryExe.class);
        when(query.execute(any())).thenReturn(PageResult.of(List.of(new PaymentRefundDTO(
                "PR1", "RF1", "PAY1", "TR1", "WECHAT", new BigDecimal("17.00"), "SUCCESS", "CHANNEL-R1")), 1));
        var mvc = MockMvcBuilders.standaloneSetup(new PaymentRefundController(query, detail))
                .setMessageConverters(converter()).build();
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
