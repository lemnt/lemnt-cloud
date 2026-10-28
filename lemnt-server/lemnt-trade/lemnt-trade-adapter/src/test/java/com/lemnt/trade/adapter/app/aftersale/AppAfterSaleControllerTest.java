package com.lemnt.trade.adapter.app.aftersale;

import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.trade.application.aftersale.executor.AfterSaleCancelCmdExe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 本人取消只取已经认证的上下文，不接收客户端会员编号。 */
class AppAfterSaleControllerTest {
    @AfterEach
    void clear() { SecurityContextHolder.remove(); }

    @Test
    void cancellationIgnoresForgedMemberInput() throws Exception {
        SecurityContextHolder.setUserId("1");
        var cancel = mock(AfterSaleCancelCmdExe.class);
        when(cancel.execute(1L, "RF1")).thenReturn(true);
        var mvc = MockMvcBuilders.standaloneSetup(new AppAfterSaleController(cancel)).build();
        mvc.perform(post("/trade/app/after-sales/RF1/cancel").header("X-User-Id", "999")
                .contentType(MediaType.APPLICATION_JSON).content("{\"memberId\":999}"))
                .andExpect(status().isOk());
        verify(cancel).execute(1L, "RF1");
        verifyNoMoreInteractions(cancel);
    }
}
