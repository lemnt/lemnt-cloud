package com.lemnt.member.domain.user.model;

import com.lemnt.common.core.exception.ServiceException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员资料投影，不携带密码字段。
 */
public record Member(
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
) {
    /**
     * 注册。
     */
    public static Member register(String memberNo, String nickname, String mobile, String email, String avatar) {
        if (memberNo == null || memberNo.isBlank()) {
            throw new ServiceException("会员编号不能为空", 400);
        }
        if (nickname == null || nickname.isBlank()) {
            throw new ServiceException("会员昵称不能为空", 400);
        }
        if ((mobile == null || mobile.isBlank()) && (email == null || email.isBlank())) {
            throw new ServiceException("手机号和邮箱至少填写一项", 400);
        }
        return new Member(null, memberNo, nickname.trim(), blankToNull(mobile), blankToNull(email),
                blankToNull(avatar), 1, 0L, 0L, null, null, null, null, null);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
