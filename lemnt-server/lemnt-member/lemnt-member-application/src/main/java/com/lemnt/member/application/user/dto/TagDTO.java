package com.lemnt.member.application.user.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 标签数据传输对象。
 */
public record TagDTO(Long id, String name, String remark, LocalDateTime createTime)
        implements Serializable {
    /** 旧投影缺少创建时间，不从其他字段推算。 */
    public TagDTO(Long id, String name, String remark) {
        this(id, name, remark, null);
    }
}
