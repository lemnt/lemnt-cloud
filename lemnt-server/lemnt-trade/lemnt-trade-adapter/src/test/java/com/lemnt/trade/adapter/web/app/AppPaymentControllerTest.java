package com.lemnt.trade.adapter.web.app;

import com.lemnt.trade.application.checkout.executor.MemberPaymentStatusQueryExe;
import com.lemnt.trade.application.checkout.service.MemberSessionService;
import com.lemnt.trade.application.checkout.qry.MemberPaymentStatusQry;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AppPaymentControllerTest {
    @Test
    void sessionIdentityOverridesCallerMemberAndForeignOrdersAreConcealed() throws Exception {
        var sessions = mock(MemberSessionService.class);
        when(sessions.requireMember("Bearer member")).thenReturn(7L);
        var query = mock(MemberPaymentStatusQueryExe.class);
        var mvc =
                MockMvcBuilders.standaloneSetup(new AppPaymentController(sessions, query)).build();
        mvc.perform(
                        get("/trade/app/orders/TR1/payment")
                                .header("Authorization", "Bearer member")
                                .header("memberId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("订单不存在"));
        verify(query).execute(new MemberPaymentStatusQry(7L, "TR1"));
    }

    @Test
    void absentMemberSessionCannotReachPaymentQuery() throws Exception {
        var sessions = mock(MemberSessionService.class);
        when(sessions.requireMember(null)).thenThrow(new SecurityException());
        var query = mock(MemberPaymentStatusQueryExe.class);
        MockMvcBuilders.standaloneSetup(new AppPaymentController(sessions, query))
                .build()
                .perform(get("/trade/app/orders/TR1/payment"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(query);
    }
}
