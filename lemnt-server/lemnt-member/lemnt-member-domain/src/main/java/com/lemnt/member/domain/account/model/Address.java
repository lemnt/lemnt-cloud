package com.lemnt.member.domain.account.model;

public record Address(
        Long id,
        Long memberId,
        String name,
        String mobile,
        Long areaId,
        String detailAddress,
        Boolean defaultStatus) {}
