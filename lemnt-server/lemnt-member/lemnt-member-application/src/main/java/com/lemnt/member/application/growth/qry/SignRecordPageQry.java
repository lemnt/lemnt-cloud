package com.lemnt.member.application.growth.qry;

import java.io.Serializable;

/**
 * 查询签到记录分页查询。
 */
public record SignRecordPageQry(
        Long memberId,
        int pageNum,
        int pageSize
) implements Serializable {}
