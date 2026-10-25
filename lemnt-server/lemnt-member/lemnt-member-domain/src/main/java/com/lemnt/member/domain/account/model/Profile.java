package com.lemnt.member.domain.account.model;

public record Profile(
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
        java.time.LocalDate birthday,
        java.time.LocalDateTime createTime) {}
