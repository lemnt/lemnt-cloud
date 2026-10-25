package com.lemnt.member.domain.account.model;

public record SignRecord(
        Long id,
        Long memberId,
        java.time.LocalDate signDate,
        Integer day,
        Long point,
        Long experience) {}
