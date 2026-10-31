package com.lemnt.member.application.growth.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 后台经验记录查询，业务类型使用源数值，不借用积分类型。 */
public record ExperienceRecordAllPageQry(
        Long memberId,
        int pageNum,
        int pageSize,
        String bizNo,
        Integer experienceBizType,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime
) implements Serializable {}
