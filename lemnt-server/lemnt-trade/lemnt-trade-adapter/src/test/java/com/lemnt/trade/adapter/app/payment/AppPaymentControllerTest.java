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
        MockMvcBuilders.standaloneSetup(new AppPaymentController(query)).addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor()).build();
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
        MockMvcBuilders.standaloneSetup(new AppPaymentController(query))
            .addInterceptors(new com.lemnt.common.security.interceptor.HeaderInterceptor()).build()
            .perform(get("/trade/app/orders/TR1/payment").header(com.lemnt.common.security.constant.SecurityConstants.HEADER_USER_ID, "8"))
            .andExpect(status().isOk());
        verify(query).execute(new MemberPaymentStatusQry(8L, "TR1"));
    }
}
