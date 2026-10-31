package com.lemnt.member.domain.user.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员资料投影，不携带密码字段。
 */
public record Member(
        Long id,
        String nickname,
        String mobile,
        String email,
        String avatar,
        Integer status,
        Long points,
        Long experience,
        Long levelId,
        Long groupId,
        Integer sex,
        LocalDate birthday,
        LocalDateTime createTime,
        String mark,
        String name,
        Integer areaId,
        LocalDateTime loginDate,
        String loginIp
) {
    /** 兼容缺少连接IP的旧投影。 */
    public Member(Long id,
        String nickname,
        String mobile,
        String email,
        String avatar,
        Integer status,
        Long points,
        Long experience,
        Long levelId,
        Long groupId,
        Integer sex,
        LocalDate birthday,
        LocalDateTime createTime,
        String mark,
        String name,
        Integer areaId,
        LocalDateTime loginDate) { this(id, nickname, mobile, email, avatar, status, points, experience, levelId, groupId, sex, birthday, createTime, mark, name, areaId, loginDate, null); }

    /** 兼容缺少登录审计的旧投影。 */
    public Member(Long id,
        String nickname,
        String mobile,
        String email,
        String avatar,
        Integer status,
        Long points,
        Long experience,
        Long levelId,
        Long groupId,
        Integer sex,
        LocalDate birthday,
        LocalDateTime createTime,
        String mark,
        String name,
        Integer areaId) { this(id, nickname, mobile, email, avatar, status, points, experience, levelId, groupId, sex, birthday, createTime, mark, name, areaId, null, null); }

    /** 兼容缺少姓名地区的既有调用。 */
    public Member(Long id,
        String nickname,
        String mobile,
        String email,
        String avatar,
        Integer status,
        Long points,
        Long experience,
        Long levelId,
        Long groupId,
        Integer sex,
        LocalDate birthday,
        LocalDateTime createTime,
        String mark) {
        this(id, nickname, mobile, email, avatar, status, points, experience, levelId, groupId, sex, birthday, createTime, mark, null, null, null, null);
    }

    /** 兼容缺少内部备注的既有调用。 */
    public Member(Long id,
        String nickname,
        String mobile,
        String email,
        String avatar,
        Integer status,
        Long points,
        Long experience,
        Long levelId,
        Long groupId,
        Integer sex,
        LocalDate birthday,
        LocalDateTime createTime) {
        this(id, nickname, mobile, email, avatar, status, points, experience, levelId, groupId, sex, birthday, createTime, null, null, null, null, null);
    }


}
