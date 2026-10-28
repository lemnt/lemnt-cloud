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
        LocalDateTime createTime
) {

}
