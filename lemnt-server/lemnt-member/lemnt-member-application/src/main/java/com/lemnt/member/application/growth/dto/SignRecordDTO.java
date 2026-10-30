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
        Long experience,
        String nickname
) implements Serializable {
    /** 无昵称的既有签到投影保持兼容。 */
    public SignRecordDTO(Long id,Long memberId,LocalDate signDate,Integer day,Long point,Long experience) {
        this(id,memberId,signDate,day,point,experience,null);
    }
}
