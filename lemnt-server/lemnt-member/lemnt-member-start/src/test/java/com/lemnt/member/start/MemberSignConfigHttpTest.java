package com.lemnt.member.start;

import com.lemnt.common.security.handler.GlobalExceptionHandler;
import com.lemnt.member.adapter.web.growth.MemberSignConfigController;
import com.lemnt.member.application.growth.cmd.SignConfigCreateCmd;
import com.lemnt.member.application.growth.cmd.SignConfigUpdateCmd;
import com.lemnt.member.application.growth.cmd.SignConfigDeleteCmd;
import com.lemnt.member.application.growth.executor.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberSignConfigHttpTest {
    /** 新增返回真实ID，修改和删除均使用路径ID，不使用day定位。 */
    @Test
    void separateCreateUpdateDeleteUseRecordId() throws Exception {
        var create = mock(MemberSignConfigCreateCmdExe.class);
        var update = mock(MemberSignConfigUpdateCmdExe.class);
        var delete = mock(MemberSignConfigDeleteCmdExe.class);
        when(create.execute(new SignConfigCreateCmd(7, 10L, 2L, 1))).thenReturn(99L);
        when(update.execute(new SignConfigUpdateCmd(99L, 3, 5L, 0L, 0))).thenReturn(true);
        when(delete.execute(new SignConfigDeleteCmd(99L))).thenReturn(true);
        var mvc = MockMvcBuilders.standaloneSetup(new MemberSignConfigController(delete,
                mock(MemberSignConfigDetailQueryExe.class), create, update, mock(MemberSignConfigListQueryExe.class)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(post("/growths/signin-configs").contentType("application/json")
                .content("{\"day\":7,\"point\":10,\"experience\":2,\"status\":1}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(99));
        mvc.perform(put("/growths/signin-configs/99").contentType("application/json")
                .content("{\"day\":3,\"point\":5,\"experience\":0,\"status\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));
        mvc.perform(delete("/growths/signin-configs/99"))
                .andExpect(status().isOk());
        verify(create).execute(new SignConfigCreateCmd(7, 10L, 2L, 1));
        verify(update).execute(new SignConfigUpdateCmd(99L, 3, 5L, 0L, 0));
        verify(delete).execute(new SignConfigDeleteCmd(99L));
    }
}
