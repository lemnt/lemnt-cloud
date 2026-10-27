package com.lemnt.member.application.user.cmd;

import java.io.Serializable;

/**
 * 会员注册命令。
 */
public record MemberRegisterCmd(
        String nickname,
        String mobile,
        String email,
        String avatar
) implements Serializable {}
