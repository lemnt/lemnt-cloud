package com.lemnt.member.domain.growth.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        String nickname,
        LocalDateTime createTime
) {
    /** 历史投影缺少真实时间，不由业务日期推算。 */
    public SignRecord(Long id, Long memberId, LocalDate signDate, Integer day, Long point, Long experience, String nickname) {
        this(id, memberId, signDate, day, point, experience, nickname, null);
    }
    /** 无昵称的既有签到投影保持兼容。 */
    public SignRecord(Long id,Long memberId,LocalDate signDate,Integer day,Long point,Long experience) {
        this(id,memberId,signDate,day,point,experience,null);
    }
}
