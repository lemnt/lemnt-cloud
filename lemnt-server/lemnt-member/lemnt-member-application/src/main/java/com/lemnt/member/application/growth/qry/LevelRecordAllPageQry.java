package com.lemnt.member.application.growth.qry;

import java.io.Serializable;

/** 等级变更流水分页查询。 */
public record LevelRecordAllPageQry(
        Long memberId,
        int pageNum,
        int pageSize
) implements Serializable {}
