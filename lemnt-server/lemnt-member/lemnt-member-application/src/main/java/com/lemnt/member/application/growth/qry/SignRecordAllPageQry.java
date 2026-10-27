package com.lemnt.member.application.growth.qry;

import java.io.Serializable;

/** 签到记录分页查询。 */
public record SignRecordAllPageQry(
        Long memberId,
        int pageNum,
        int pageSize
) implements Serializable {}
