package com.lemnt.member.domain.user.model;

/** 分组分页筛选条件。 */
public record GroupPageFilter(
        String name,
        Integer status,
        int offset,
        int limit
) {}
