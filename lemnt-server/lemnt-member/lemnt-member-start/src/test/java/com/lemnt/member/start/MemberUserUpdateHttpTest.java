package com.lemnt.member.start;

import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.interceptor.HeaderInterceptor;
import com.lemnt.member.adapter.web.user.MemberUserController;
import com.lemnt.member.adapter.app.user.AppMemberUserController;
import com.lemnt.member.application.user.cmd.MemberUserUpdateCmd;
import com.lemnt.member.application.user.cmd.MemberUpdateCmd;
import com.lemnt.member.application.user.executor.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberUserUpdateHttpTest {
    /** 后台改号进入独立命令，App附加后台字段不会进入应用命令。 */
    @Test
    void webMobileAndAppProfileUseSeparateCommands() throws Exception {
        var web = mock(MemberUserUpdateCmdExe.class);
        var app = mock(MemberUpdateCmdExe.class);
        when(web.execute(any())).thenReturn(true);
        when(app.execute(any())).thenReturn(true);
        var mvc = MockMvcBuilders.standaloneSetup(
                new MemberUserController(mock(MemberUserDetailQueryExe.class), web, mock(MemberUserPageQueryExe.class)),
                new AppMemberUserController(mock(MemberDetailQueryExe.class), app))
                .addInterceptors(new HeaderInterceptor()).build();
        mvc.perform(put("/member-users/2").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\":\"web\",\"mobile\":\"13800000099\",\"status\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(true));
        verify(web).execute(new MemberUserUpdateCmd(2L, "web", null, null, null, null, 0, null, null, null, "13800000099"));
        mvc.perform(put("/app/users/user").header(SecurityConstants.HEADER_USER_ID, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\":\"app\",\"mobile\":\"13800000099\",\"status\":0,\"memberId\":2}"))
                .andExpect(status().isOk());
        verify(app).execute(new MemberUpdateCmd(1L, "app", null, null, null, null, null));
        verifyNoMoreInteractions(web, app);
    }

    /** 过长手机号在HTTP边界拒绝，旧后台请求省略手机号仍可进入原资料保存。 */
    @Test
    void rejectsTooLongMobileAndAllowsOmission() throws Exception {
        var web = mock(MemberUserUpdateCmdExe.class);
        var mvc = MockMvcBuilders.standaloneSetup(new MemberUserController(
                mock(MemberUserDetailQueryExe.class), web, mock(MemberUserPageQueryExe.class))).build();
        mvc.perform(put("/member-users/2").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\":\"web\",\"mobile\":\"" + "1".repeat(25) + "\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(web);
        mvc.perform(put("/member-users/2").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\":\"web\"}")).andExpect(status().isOk());
        verify(web).execute(new MemberUserUpdateCmd(2L, "web", null, null, null, null, null, null, null, null, null));
    }
}
