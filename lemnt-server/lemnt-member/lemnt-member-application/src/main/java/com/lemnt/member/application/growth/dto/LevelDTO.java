package com.lemnt.member.application.growth.dto;

import java.io.Serializable;

/**
 * 等级数据传输对象。
 */
public record LevelDTO(
        Long id,
        String name,
        Integer level,
        Long experience,
        Integer discountPercent,
        String icon,
        String backgroundUrl,
        Integer status
) implements Serializable {}
