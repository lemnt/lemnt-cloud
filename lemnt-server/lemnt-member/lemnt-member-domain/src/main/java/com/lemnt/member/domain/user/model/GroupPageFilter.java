package com.lemnt.member.domain.user.model;

import java.time.LocalDateTime;

/** 分组分页筛选条件。 */
public record GroupPageFilter(
        String name,
        Integer status,
        int offset,
        int limit,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
    /** 旧查询不附加创建时间。 */
    public GroupPageFilter(String name, Integer status, int offset, int limit) { this(name, status, offset, limit, null, null); }
}
