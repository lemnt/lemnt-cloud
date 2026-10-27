package com.lemnt.member.application.user.cmd;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 资料更新命令。
 */
public record MemberUpdateCmd(
        Long memberId,
        String nickname,
        String email,
        String avatar,
        Integer sex,
        LocalDate birthday,
        Integer status
) implements Serializable {}
