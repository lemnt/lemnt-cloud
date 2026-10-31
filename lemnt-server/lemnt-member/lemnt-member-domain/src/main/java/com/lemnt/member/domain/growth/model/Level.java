package com.lemnt.member.domain.growth.model;

/**
 * 等级。
 */
public record Level(
        Long id,
        String name,
        Integer level,
        Long experience,
        Integer discountPercent,
        String icon,
        String backgroundUrl,
        Integer status
) {}
