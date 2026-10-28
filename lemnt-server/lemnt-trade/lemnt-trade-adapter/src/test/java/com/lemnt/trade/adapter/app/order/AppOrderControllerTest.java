package com.lemnt.trade.adapter.app.order;

import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.trade.application.fulfillment.executor.OrderReceiveCmdExe;
import com.lemnt.trade.application.order.executor.TradeOrderCloseCmdExe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 控制器只取认证上下文中的会员编号，不接收Body或伪造身份头。 */
class AppOrderControllerTest {
    @AfterEach
    void cleanup() { SecurityContextHolder.remove(); }

    @Test
    void cancelAndReceiveUseVerifiedMemberContext() throws Exception {
        SecurityContextHolder.setUserId("1");
        var close = mock(TradeOrderCloseCmdExe.class);
        var receive = mock(OrderReceiveCmdExe.class);
        when(close.executeForMember(1L, "TR1")).thenReturn(true);
        var mvc = MockMvcBuilders.standaloneSetup(new AppOrderController(close, receive)).build();
        mvc.perform(post("/trade/app/orders/TR1/cancel").header("X-User-Id", "999")
                .contentType(MediaType.APPLICATION_JSON).content("{\"memberId\":999,\"userType\":2}"))
                .andExpect(status().isOk());
        mvc.perform(post("/trade/app/orders/TR1/receive").header("X-User-Id", "999"))
                .andExpect(status().isOk());
        verify(close).executeForMember(1L, "TR1");
        verify(receive).executeForMember(1L, "TR1");
        verifyNoMoreInteractions(close, receive);
    }
}
