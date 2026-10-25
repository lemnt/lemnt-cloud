package com.lemnt.member.application.account.dto;

public record SignRecordDTO(
        Long id,
        Long memberId,
        java.time.LocalDate signDate,
        Integer day,
        Long point,
        Long experience) {}
