package com.lemnt.member.domain.user.model;

import java.time.LocalDateTime;

/**
 * 分组。
 */
public record Group(
        Long id,
        String name,
        String remark,
        Integer status,
        LocalDateTime createTime
) {
    /** 旧投影缺少创建时间，不从其他字段推算。 */
    public Group(Long id, String name, String remark, Integer status) { this(id, name, remark, status, null); }
}
