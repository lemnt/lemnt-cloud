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
        Long experience,
        String nickname
) {
    /** 无昵称的既有签到投影保持兼容。 */
    public SignRecord(Long id,Long memberId,LocalDate signDate,Integer day,Long point,Long experience) {
        this(id,memberId,signDate,day,point,experience,null);
    }
}
