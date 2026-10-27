package com.lemnt.member.application.user.dto;

import java.io.Serializable;

/**
 * 分组数据传输对象。
 */
public record GroupDTO(
        Long id,
        String name,
        String remark,
        Integer status
) implements Serializable {}
