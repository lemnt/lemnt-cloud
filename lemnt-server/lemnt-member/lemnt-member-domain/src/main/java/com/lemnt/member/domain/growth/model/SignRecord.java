package com.lemnt.member.domain.growth.model;

import java.time.LocalDate;

/**
 * 签到记录。
 */
public record SignRecord(
        Long id,
        Long memberId,
        LocalDate signDate,
        Integer day,
        Long point,
        Long experience
) {}
