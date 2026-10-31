package com.lemnt.member.application.user.dto;

import java.time.LocalDateTime;

import java.io.Serializable;

/**
 * 分组数据传输对象。
 */
public record GroupDTO(
        Long id,
        String name,
        String remark,
        Integer status,
        LocalDateTime createTime
) implements Serializable {
    /** 旧投影缺少创建时间，不从其他字段推算。 */
    public GroupDTO(Long id, String name, String remark, Integer status) { this(id, name, remark, status, null); }
}
