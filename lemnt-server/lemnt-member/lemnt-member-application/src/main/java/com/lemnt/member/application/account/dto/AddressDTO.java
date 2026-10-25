package com.lemnt.member.application.account.dto;

public record AddressDTO(
        Long id,
        Long memberId,
        String name,
        String mobile,
        Long areaId,
        String detailAddress,
        Boolean defaultStatus) {}
