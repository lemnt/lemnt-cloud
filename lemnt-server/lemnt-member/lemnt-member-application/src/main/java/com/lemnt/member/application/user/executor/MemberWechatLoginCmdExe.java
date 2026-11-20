package com.lemnt.member.application.user.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.application.user.cmd.MemberWechatLoginCmd;
import com.lemnt.member.domain.user.gateway.MemberWechatGateway;
import com.lemnt.member.domain.user.gateway.WechatLoginGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 会员微信登录：换取 openid 后绑定到当前会员，供小程序发起微信支付使用。
 */
@Component
@RequiredArgsConstructor
public class MemberWechatLoginCmdExe {

    private static final int MAX_CODE_LENGTH = 128;

    private final WechatLoginGateway wechatLoginGateway;
    private final MemberWechatGateway memberWechatGateway;

    public Boolean execute(MemberWechatLoginCmd cmd) {
        require(cmd != null && cmd.memberId() != null && cmd.memberId() > 0, "会员未登录");
        require(
            cmd.code() != null && !cmd.code().isBlank() && cmd.code().length() <= MAX_CODE_LENGTH,
            "微信登录凭证不合法");
        String openId = wechatLoginGateway.exchangeOpenId(cmd.code());
        memberWechatGateway.bind(cmd.memberId(), openId);
        return true;
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }
}
