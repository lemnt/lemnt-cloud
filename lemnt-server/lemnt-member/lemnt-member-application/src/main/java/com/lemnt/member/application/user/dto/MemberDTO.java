package com.lemnt.member.application.user.dto;

import java.util.List;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资料数据传输对象。
 */
public record MemberDTO(
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
        String loginIp,
        List<Long> tagIds,
        List<String> tagNames,
        String levelName,
        String groupName
) implements Serializable {
    /** 基础查询未读取关系时保持未知，不把未知当作无绑定。 */
    public MemberDTO(Long id, String nickname, String mobile, String email, String avatar, Integer status,
            Long points, Long experience, Long levelId, Long groupId, Integer sex, LocalDate birthday,
            LocalDateTime createTime, String mark, String name, Integer areaId, LocalDateTime loginDate, String loginIp) {
        this(id, nickname, mobile, email, avatar, status, points, experience, levelId, groupId, sex, birthday,
                createTime, mark, name, areaId, loginDate, loginIp, null, null, null, null);
    }

    /** 兼容缺少连接IP的旧投影。 */
    public MemberDTO(Long id,
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
    public MemberDTO(Long id,
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
    public MemberDTO(Long id,
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
    public MemberDTO(Long id,
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
