package com.lemnt.member.application.user.qry;

import java.time.LocalDateTime;

import java.io.Serializable;

/** 标签分页查询。 */
public record TagPageQry(
        String name,
        int pageNum,
        int pageSize,
        LocalDateTime startTime,
        LocalDateTime endTime
) implements Serializable {
    /** 旧查询不附加创建时间。 */
    public TagPageQry(String name, int pageNum, int pageSize) { this(name, pageNum, pageSize, null, null); }
}
