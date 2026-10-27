package com.lemnt.member.application.user.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资料数据传输对象。
 */
public record MemberDTO(
        Long id,
        String memberNo,
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
        LocalDateTime createTime
) implements Serializable {}
