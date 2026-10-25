package com.lemnt.member.application.account.cmd;

public record ProfileUpdateCmd(
        Long memberId,
        String nickname,
        String email,
        String avatar,
        Integer sex,
        java.time.LocalDate birthday,
        Integer status) {}
