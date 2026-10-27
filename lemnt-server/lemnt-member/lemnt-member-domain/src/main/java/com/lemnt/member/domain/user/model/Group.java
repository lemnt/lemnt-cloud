package com.lemnt.member.domain.user.model;

/**
 * 分组。
 */
public record Group(
        Long id,
        String name,
        String remark,
        Integer status
) {}
