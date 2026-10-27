package com.lemnt.member.application.growth.dto;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 签到记录数据传输对象。
 */
public record SignRecordDTO(
        Long id,
        Long memberId,
        LocalDate signDate,
        Integer day,
        Long point,
        Long experience
) implements Serializable {}
