package com.lemnt.member.start;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.application.user.cmd.MemberWechatLoginCmd;
import com.lemnt.member.application.user.executor.MemberWechatLoginCmdExe;
import com.lemnt.member.domain.user.gateway.MemberWechatGateway;
import com.lemnt.member.domain.user.gateway.WechatLoginGateway;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 会员微信登录：凭证换 openid 后绑定会员，入参不合法时不访问渠道。
 */
class MemberWechatLoginCmdExeTest {
    final WechatLoginGateway wechatLogin = mock(WechatLoginGateway.class);
    final MemberWechatGateway memberWechat = mock(MemberWechatGateway.class);
    final MemberWechatLoginCmdExe exe =
        new MemberWechatLoginCmdExe(wechatLogin, memberWechat);

    @Test
    void validCodeBindsOpenIdToCurrentMember() {
        when(wechatLogin.exchangeOpenId("CODE1")).thenReturn("OPENID1");

        assertTrue(exe.execute(new MemberWechatLoginCmd(7L, "CODE1")));

        verify(memberWechat).bind(7L, "OPENID1");
    }

    @Test
    void blankCodeOrMissingMemberIsRejectedBeforeCallingChannel() {
        assertThrows(
            ServiceException.class, () -> exe.execute(new MemberWechatLoginCmd(7L, " ")));
        assertThrows(
            ServiceException.class, () -> exe.execute(new MemberWechatLoginCmd(null, "CODE1")));
        verifyNoInteractions(wechatLogin, memberWechat);
    }
}
