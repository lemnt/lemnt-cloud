package com.lemnt.member.start;

import com.lemnt.member.adapter.web.account.*;
import com.lemnt.member.application.account.service.MemberAuthService;
import com.lemnt.member.application.account.executor.*;
import com.lemnt.member.application.account.qry.AddressListQry;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberHttpContractTest {
    @Test
    void accountIdentityComesFromTokenAndRejectsMissingSession() throws Exception {
        var auth = mock(MemberAuthService.class);
        var commands = mock(MemberAccountCmdExe.class);
        var queries = mock(MemberAccountQueryExe.class);
        when(auth.require("Bearer member")).thenReturn(7L);
        when(auth.require(null)).thenThrow(new SecurityException("会员会话无效"));
        when(queries.execute(new AddressListQry(7L))).thenReturn(java.util.List.of());
        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new AppMemberAddressController(auth, commands, queries))
                        .setControllerAdvice(new MemberAccountExceptionHandler())
                        .build();
        mvc.perform(
                        get("/member/app/addresses")
                                .header("Authorization", "Bearer member")
                                .header("memberId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
        verify(queries).execute(new AddressListQry(7L));
        mvc.perform(get("/member/app/addresses")).andExpect(status().isUnauthorized());
        verifyNoInteractions(commands);
    }

    @Test
    void malformedSocialLoginCannotReachProviderAndUnconfiguredProviderIs503() throws Exception {
        var auth = mock(MemberAuthService.class);
        var commands = mock(MemberExternalCmdExe.class);
        var queries = mock(MemberExternalQueryExe.class);
        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new AppMemberExternalController(auth, commands, queries))
                        .setControllerAdvice(new MemberAccountExceptionHandler())
                        .build();
        mvc.perform(
                        post("/member/app/auth/social-login")
                                .contentType("application/json")
                                .content(
                                        "{\"provider\":\"WECHAT_MINI\",\"loginCode\":\"\",\"state\":\"\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(commands);
        when(commands.execute(any(com.lemnt.member.application.account.cmd.SocialLoginCmd.class)))
                .thenThrow(new UnsupportedOperationException());
        mvc.perform(
                        post("/member/app/auth/social-login")
                                .contentType("application/json")
                                .content(
                                        "{\"provider\":\"WECHAT_MINI\",\"loginCode\":\"code\",\"state\":\"state\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.msg").value("所需会员外部服务尚未配置"));
    }
}
