package com.lemnt.member.domain.address.model;

/**
 * 地址。
 */
public record Address(
        Long id,
        Long memberId,
        String name,
        String mobile,
        Long areaId,
        String detailAddress,
        Boolean defaultStatus
) {}
