package com.lemnt.member.application.user.cmd;

import java.io.Serializable;

/**
 * 会员绑定微信小程序标识命令，code 为 wx.login 返回的一次性凭证。
 */
public record MemberWechatLoginCmd(Long memberId, String code) implements Serializable {
}
