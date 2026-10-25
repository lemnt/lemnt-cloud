package com.lemnt.member.application.account.dto;

public record LevelDTO(
        Long id,
        String name,
        Integer level,
        Long experience,
        Integer discountPercent,
        String icon,
        String backgroundUrl,
        Integer status) {}
