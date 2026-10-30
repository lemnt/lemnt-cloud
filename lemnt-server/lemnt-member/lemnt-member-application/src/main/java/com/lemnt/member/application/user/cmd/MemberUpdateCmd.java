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
        Integer status,
        String mark,
        String name,
        Integer areaId
) implements Serializable {
    /** 兼容缺少姓名地区的既有调用。 */
    public MemberUpdateCmd(Long memberId,
        String nickname,
        String email,
        String avatar,
        Integer sex,
        LocalDate birthday,
        Integer status,
        String mark) {
        this(memberId, nickname, email, avatar, sex, birthday, status, mark, null, null);
    }

    /** 兼容缺少内部备注的既有调用。 */
    public MemberUpdateCmd(Long memberId,
        String nickname,
        String email,
        String avatar,
        Integer sex,
        LocalDate birthday,
        Integer status) {
        this(memberId, nickname, email, avatar, sex, birthday, status, null, null, null);
    }
}
