package com.lemnt.member.application.user.qry;

import java.io.Serializable;

/** 分组分页查询。 */
public record GroupPageQry(
        String name,
        Integer status,
        int pageNum,
        int pageSize
) implements Serializable {}
