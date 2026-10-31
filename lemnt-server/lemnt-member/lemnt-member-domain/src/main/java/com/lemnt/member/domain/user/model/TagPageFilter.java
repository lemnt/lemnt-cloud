package com.lemnt.member.domain.user.model;

import java.time.LocalDateTime;

/** 标签分页筛选条件。 */
public record TagPageFilter(
        String name,
        int offset,
        int limit,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
    /** 旧查询不附加创建时间。 */
    public TagPageFilter(String name, int offset, int limit) { this(name, offset, limit, null, null); }
}
