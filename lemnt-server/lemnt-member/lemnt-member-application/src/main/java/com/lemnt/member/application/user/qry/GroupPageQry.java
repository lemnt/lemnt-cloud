package com.lemnt.member.application.user.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 分组分页查询。 */
public record GroupPageQry(
        String name,
        Integer status,
        int pageNum,
        int pageSize,
        LocalDateTime startTime,
        LocalDateTime endTime)
        implements Serializable {
    /** 旧查询不附加创建时间。 */
    public GroupPageQry(String name, Integer status, int pageNum, int pageSize) {
        this(name, status, pageNum, pageSize, null, null);
    }
}
