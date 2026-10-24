package com.lemnt.member.domain.model;

import java.time.LocalDateTime;

/** 会员基础领域对象；认证凭据与会员资料分离。 */
public record Member(Long id, String memberNo, String nickname, String mobile, String email,
                     String avatar, Integer status, Long points, LocalDateTime createTime) {
    public static Member register(String memberNo, String nickname, String mobile, String email, String avatar) {
        if (memberNo == null || memberNo.isBlank()) throw new IllegalArgumentException("会员编号不能为空");
        if (nickname == null || nickname.isBlank()) throw new IllegalArgumentException("会员昵称不能为空");
        if ((mobile == null || mobile.isBlank()) && (email == null || email.isBlank()))
            throw new IllegalArgumentException("手机号和邮箱至少填写一项");
        return new Member(null, memberNo, nickname.trim(), blankToNull(mobile), blankToNull(email),
                blankToNull(avatar), 1, 0L, null);
    }
    private static String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
}