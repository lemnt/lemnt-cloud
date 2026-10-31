package com.lemnt.member.application.growth.qry;

import java.io.Serializable;

/**
 * 等级记录分页查询。
 */
public record LevelRecordsPageQry(
        Long memberId,
        int pageNum,
        int pageSize
) implements Serializable {}
