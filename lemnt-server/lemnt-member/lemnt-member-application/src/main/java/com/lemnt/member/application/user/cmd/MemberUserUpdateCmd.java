package com.lemnt.member.application.user.cmd;

import java.io.Serializable;
import java.time.LocalDate;

/** 后台会员资料更新命令，手机号不对App开放。 */
public record MemberUserUpdateCmd(
        Long memberId,
        String nickname,
        String email,
        String avatar,
        Integer sex,
        LocalDate birthday,
        Integer status,
        String mark,
        String name,
        Integer areaId,
        String mobile
) implements Serializable {}
