package com.lemnt.member.domain.user.model;

import java.time.LocalDateTime;

/**
 * 标签。
 */
public record Tag(
        Long id,
        String name,
        String remark,
        LocalDateTime createTime
) {
    /** 旧投影缺少创建时间，不从其他字段推算。 */
    public Tag(Long id, String name, String remark) { this(id, name, remark, null); }
}
