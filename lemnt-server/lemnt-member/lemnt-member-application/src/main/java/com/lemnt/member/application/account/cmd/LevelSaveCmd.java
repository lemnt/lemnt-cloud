package com.lemnt.member.application.account.cmd;

public record LevelSaveCmd(
        Long id,
        String name,
        Integer level,
        Long experience,
        Integer discountPercent,
        String icon,
        String backgroundUrl,
        Integer status) {}
