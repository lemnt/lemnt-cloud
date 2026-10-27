package com.lemnt.member.application.user.dto;

import java.io.Serializable;

/**
 * 标签数据传输对象。
 */
public record TagDTO(
        Long id,
        String name,
        String remark
) implements Serializable {}
