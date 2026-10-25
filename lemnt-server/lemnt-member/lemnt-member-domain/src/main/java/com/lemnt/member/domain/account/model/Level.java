package com.lemnt.member.domain.account.model;

public record Level(
        Long id,
        String name,
        Integer level,
        Long experience,
        Integer discountPercent,
        String icon,
        String backgroundUrl,
        Integer status) {}
