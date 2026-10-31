package com.lemnt.trade.adapter.app.payment;

import com.lemnt.trade.application.payment.executor.MemberPaymentStatusQueryExe;
import com.lemnt.trade.application.payment.qry.MemberPaymentStatusQry;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppPaymentControllerTest {
    @Test
    void sessionIdentityOverridesCallerMemberAndForeignOrdersAreConcealed() throws Exception {
        var query = mock(MemberPaymentStatusQueryExe.class);
        var mvc =
        MockMvcBuilders.standaloneSetup(new AppPaymentController(query, mock(com.lemnt.trade.application.payment.executor.PaymentPrepayCmdExe.class))).addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor()).build();
        mvc.perform(
                get("/trade/app/orders/TR1/payment")
                .header("Authorization", "Bearer member")
                .header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, "7")
                .header("memberId", "999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.msg").value("订单不存在"));
        verify(query).execute(new MemberPaymentStatusQry(7L, "TR1"));
    }

    @Test
    void gatewayIdentityWorksWithoutAuthorizationHeader() throws Exception {
        var query = mock(MemberPaymentStatusQueryExe.class);
        MockMvcBuilders.standaloneSetup(new AppPaymentController(query, mock(com.lemnt.trade.application.payment.executor.PaymentPrepayCmdExe.class)))
            .addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor()).build()
            .perform(get("/trade/app/orders/TR1/payment").header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, "8"))
            .andExpect(status().isOk());
        verify(query).execute(new MemberPaymentStatusQry(8L, "TR1"));
    }

    @Test
    void prepayUsesSessionMemberAndReturnsChannelParams() throws Exception {
        var query = mock(MemberPaymentStatusQueryExe.class);
        var prepay = mock(com.lemnt.trade.application.payment.executor.PaymentPrepayCmdExe.class);
        org.mockito.Mockito.when(prepay.execute(any())).thenReturn(
                new com.lemnt.trade.application.payment.dto.PaymentPrepayDTO("PAY1", "TR1", "SANDBOX",
                        new java.math.BigDecimal("8.00"), null,
                        java.util.Map.of("paymentNo", "PAY1", "appKey", "lemnt-mall")));
        var mvc = MockMvcBuilders.standaloneSetup(new AppPaymentController(query, prepay))
                .addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor()).build();
        mvc.perform(post("/trade/app/orders/TR1/payment")
                        .header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, "7")
                        .contentType("application/json")
                        .content("{\"appKey\":\"lemnt-mall\",\"channel\":\"SANDBOX\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentNo").value("PAY1"))
                .andExpect(jsonPath("$.data.channel").value("SANDBOX"))
                .andExpect(jsonPath("$.data.amount").value(8.0))
                .andExpect(jsonPath("$.data.params.appKey").value("lemnt-mall"));
        verify(prepay).execute(new com.lemnt.trade.application.payment.cmd.PaymentPrepayCmd(
                7L, "TR1", "lemnt-mall", "SANDBOX"));
    }
}
