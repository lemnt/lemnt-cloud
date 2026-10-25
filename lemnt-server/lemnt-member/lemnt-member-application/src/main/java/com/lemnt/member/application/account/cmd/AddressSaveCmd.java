package com.lemnt.member.application.account.cmd;

public record AddressSaveCmd(
        Long id,
        Long memberId,
        String name,
        String mobile,
        Long areaId,
        String detailAddress,
        Boolean defaultStatus) {}
