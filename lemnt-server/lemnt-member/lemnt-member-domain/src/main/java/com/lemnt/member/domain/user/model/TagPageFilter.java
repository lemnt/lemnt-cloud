package com.lemnt.member.domain.user.model;

/** 标签分页筛选条件。 */
public record TagPageFilter(
        String name,
        int offset,
        int limit
) {}
